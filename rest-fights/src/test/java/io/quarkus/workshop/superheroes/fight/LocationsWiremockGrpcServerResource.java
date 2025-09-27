package io.quarkus.workshop.superheroes.fight;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.wiremock.grpc.GrpcExtensionFactory;
import org.wiremock.grpc.dsl.WireMockGrpcService;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager.TestInjector.AnnotatedAndMatchesType;

import io.quarkus.workshop.superheroes.location.grpc.LocationsGrpc;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

/**
 * Quarkus {@link QuarkusTestResourceLifecycleManager} wrapping a {@link WireMockServer}, {@link WireMock}, or {@link WireMockGrpcService}, while binding its base url to the locations services, and exposing it to tests that want to inject it via {@link InjectGrpcWireMock}.
 *
 * @see InjectGrpcWireMock
 */
public class LocationsWiremockGrpcServerResource implements QuarkusTestResourceLifecycleManager {
  private final WireMockServer wireMockServer;

  public LocationsWiremockGrpcServerResource() {
    // WireMock gRPC expects proto files in target/test-classes/wiremock/grpc/
    // The proto file should be in src/test/resources/wiremock/grpc/ which gets
    // copied to target/test-classes/wiremock/grpc/ during the build
    var wiremockRoot = "target/test-classes/wiremock";
    var wiremockDir = new File(wiremockRoot);
    var grpcDir = new File(wiremockDir, "grpc");
    
    // Ensure directory exists and proto file is present
    // This MUST happen before WireMockServer is created
    try {
      Files.createDirectories(grpcDir.toPath());
      
      // WireMock gRPC needs descriptor files (.dsc), not just .proto files
      // First try to copy descriptor file from test resources (most reliable)
      var descriptorFile = new File(grpcDir, "locationservice-v1.dsc");
      var protoFile = new File(grpcDir, "locationservice-v1.proto");
      
      if (!descriptorFile.exists()) {
        // Try to copy descriptor file from test resources
        try (InputStream dscStream = getClass().getClassLoader()
            .getResourceAsStream("wiremock/grpc/locationservice-v1.dsc")) {
          if (dscStream != null) {
            Files.copy(dscStream, descriptorFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
          }
        } catch (Exception e) {
          // If descriptor not in resources, we'll generate it below
        }
      }
      
      // If descriptor still doesn't exist, generate it from proto file
      if (!descriptorFile.exists()) {
        // First ensure proto file exists
        if (!protoFile.exists()) {
          // Try from test resources first
          try (InputStream protoStream = getClass().getClassLoader()
              .getResourceAsStream("wiremock/grpc/locationservice-v1.proto")) {
            if (protoStream != null) {
              Files.copy(protoStream, protoFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
              // Fallback to main proto directory
              File mainProtoFile = new File("src/main/proto/locationservice-v1.proto");
              if (!mainProtoFile.exists()) {
                mainProtoFile = new File("rest-fights/src/main/proto/locationservice-v1.proto");
              }
              if (mainProtoFile.exists() && mainProtoFile.canRead()) {
                Files.copy(mainProtoFile.toPath(), protoFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
              }
            }
          }
        }
        
        // Generate descriptor file from proto using protoc
        if (protoFile.exists()) {
          generateDescriptorFile(protoFile, descriptorFile, grpcDir);
        }
      }
      
      // Verify descriptor file exists (WireMock gRPC needs this)
      if (!descriptorFile.exists()) {
        throw new RuntimeException("Descriptor file not found at: " + descriptorFile.getAbsolutePath() + 
            ". WireMock gRPC requires .dsc files. Ensure src/test/resources/wiremock/grpc/locationservice-v1.dsc exists or protoc is available.");
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to set up WireMock gRPC proto file: " + e.getMessage(), e);
    }

    // Create WireMockServer with absolute path to ensure WireMock can find the proto files
    // GrpcExtensionFactory scans the grpc subdirectory for proto files during initialization
    var absoluteWiremockRoot = wiremockDir.getAbsolutePath();
    this.wireMockServer =
      new WireMockServer(
        wireMockConfig()
          .dynamicPort()
          .withRootDirectory(absoluteWiremockRoot)
          .extensions(new GrpcExtensionFactory())
      );
  }

  @Override
  public Map<String, String> start() {
    this.wireMockServer.start();

    var port = getPort();

    return Map.of(
      "quarkus.grpc.clients.locations.host", "localhost",
      "quarkus.grpc.clients.locations.port", String.valueOf(port),
      "quarkus.grpc.clients.locations.test-port", String.valueOf(port)
    );
  }

  @Override
  public void stop() {
    this.wireMockServer.stop();
  }

  @Override
  public void inject(TestInjector testInjector) {
    testInjector.injectIntoFields(
      new WireMockGrpcService(new WireMock(getPort()), LocationsGrpc.SERVICE_NAME),
      new AnnotatedAndMatchesType(InjectGrpcWireMock.class, WireMockGrpcService.class)
    );
  }

  private int getPort() {
    return this.wireMockServer.isHttpsEnabled() ?
           this.wireMockServer.httpsPort() :
           this.wireMockServer.port();
  }

  /**
   * Generates a descriptor file (.dsc) from a proto file using protoc.
   * This is required for WireMock gRPC to discover service methods.
   */
  private void generateDescriptorFile(File protoFile, File descriptorFile, File grpcDir) {
    try {
      // Find protoc executable
      String protocPath = findProtoc();
      if (protocPath == null) {
        throw new RuntimeException("protoc not found. Please install Protocol Buffers compiler.");
      }

      // Build protoc command
      List<String> command = new ArrayList<>();
      command.add(protocPath);
      command.add("--descriptor_set_out=" + descriptorFile.getAbsolutePath());
      command.add("--include_imports");
      command.add(protoFile.getAbsolutePath());

      // Execute protoc
      ProcessBuilder pb = new ProcessBuilder(command);
      pb.directory(grpcDir);
      Process process = pb.start();
      int exitCode = process.waitFor();

      if (exitCode != 0) {
        // Read error output
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getErrorStream()))) {
          String error = reader.lines().collect(Collectors.joining("\n"));
          throw new RuntimeException("protoc failed with exit code " + exitCode + ": " + error);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate descriptor file: " + e.getMessage(), e);
    }
  }

  /**
   * Finds the protoc executable in the system PATH or common locations.
   */
  private String findProtoc() {
    // Try common locations
    String[] commonPaths = {
      "/usr/local/bin/protoc",
      "/usr/bin/protoc",
      "/opt/homebrew/bin/protoc",
      System.getenv("PATH") != null ? "protoc" : null
    };

    for (String path : commonPaths) {
      if (path == null) continue;
      
      try {
        File protocFile = new File(path);
        if (protocFile.exists() && protocFile.canExecute()) {
          return protocFile.getAbsolutePath();
        }
        
        // Try as command in PATH
        if (path.equals("protoc")) {
          ProcessBuilder pb = new ProcessBuilder("which", "protoc");
          Process process = pb.start();
          if (process.waitFor() == 0) {
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
              String result = reader.lines().collect(Collectors.joining()).trim();
              if (!result.isEmpty() && new File(result).exists()) {
                return result;
              }
            }
          }
        }
      } catch (Exception e) {
        // Continue searching
      }
    }
    
    return null;
  }
}
