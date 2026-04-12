package io.quarkus.workshop.superheroes.fight.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.wiremock.grpc.dsl.WireMockGrpc.*;

import java.time.Duration;
import java.util.stream.IntStream;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wiremock.grpc.dsl.WireMockGrpcService;

import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

import io.quarkus.workshop.superheroes.fight.FightLocation;
import io.quarkus.workshop.superheroes.fight.InjectGrpcWireMock;
import io.quarkus.workshop.superheroes.fight.LocationsWiremockGrpcServerResource;
import io.quarkus.workshop.superheroes.location.grpc.HelloReply;
import io.quarkus.workshop.superheroes.location.grpc.HelloRequest;
import io.quarkus.workshop.superheroes.location.grpc.Location;
import io.quarkus.workshop.superheroes.location.grpc.LocationType;
import io.quarkus.workshop.superheroes.location.grpc.RandomLocationRequest;

import io.smallrye.faulttolerance.api.CircuitBreakerMaintenance;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;

/**
 * WireMock gRPC tests for {@link LocationClient} (happy paths and NOT_FOUND recovery).
 * <p>
 *   The {@code findRandomLocation} circuit-breaker <em>open</em> behaviour is asserted in
 *   {@link LocationClientFindRandomLocationCircuitBreakerTest} under the default breaker limits. This class uses
 *   {@link LocationClientTestProfiles.HighCircuitBreakerVolumeForFindRandomLocation} so other tests in the suite cannot
 *   leave the breaker open before these stubs run.
 * </p>
 */
@QuarkusTest
@TestProfile(LocationClientTestProfiles.HighCircuitBreakerVolumeForFindRandomLocation.class)
@WithTestResource(LocationsWiremockGrpcServerResource.class)
class LocationClientTests {
  private static final String DEFAULT_HELLO_RESPONSE = "Hello locations!";
  private static final String DEFAULT_LOCATION_NAME = "Gotham City";
  private static final String DEFAULT_LOCATION_DESCRIPTION = "Dark city where Batman lives.";
  private static final String DEFAULT_LOCATION_PICTURE = "gotham_city.png";
  private static final LocationType DEFAULT_LOCATION_TYPE = LocationType.PLANET;
  private static final Location DEFAULT_LOCATION = Location.newBuilder()
    .setName(DEFAULT_LOCATION_NAME)
    .setDescription(DEFAULT_LOCATION_DESCRIPTION)
    .setPicture(DEFAULT_LOCATION_PICTURE)
    .setType(DEFAULT_LOCATION_TYPE)
    .build();

  private static final FightLocation DEFAULT_FIGHT_LOCATION = new FightLocation(
    DEFAULT_LOCATION_NAME,
    DEFAULT_LOCATION_DESCRIPTION,
    DEFAULT_LOCATION_PICTURE
  );

  @Inject
  LocationClient locationClient;

  @InjectGrpcWireMock
  WireMockGrpcService wireMockGrpc;

  @Inject
  CircuitBreakerMaintenance circuitBreakerMaintenance;

  @BeforeEach
  void beforeEach() {
    this.circuitBreakerMaintenance.resetAll();
    this.wireMockGrpc.resetAll();
  }

  @AfterEach
  void afterEach() {
    this.circuitBreakerMaintenance.resetAll();
  }

  @Test
  void helloLocations() {
    this.wireMockGrpc.stubFor(
      method("Hello")
        .willReturn(message(HelloReply.newBuilder().setMessage(DEFAULT_HELLO_RESPONSE)))
    );

    this.locationClient.helloLocations()
      .subscribe().withSubscriber(UniAssertSubscriber.create())
      .assertSubscribed()
      .awaitItem(Duration.ofSeconds(5))
      .assertItem(DEFAULT_HELLO_RESPONSE);

    this.wireMockGrpc.verify(1, "Hello")
      .withRequestMessage(equalToMessage(HelloRequest.newBuilder()));
  }

  @Test
  void findsRandom() {
    this.wireMockGrpc.stubFor(
      method("GetRandomLocation")
        .willReturn(message(DEFAULT_LOCATION))
    );

    IntStream.range(0, 5)
      .forEach(i -> {
        var location = this.locationClient.findRandomLocation()
          .subscribe().withSubscriber(UniAssertSubscriber.create())
          .assertSubscribed()
          .awaitItem(Duration.ofSeconds(10))
          .getItem();

        assertThat(location)
          .isNotNull()
          .isEqualTo(DEFAULT_FIGHT_LOCATION);
      });

    this.wireMockGrpc.verify(5, "GetRandomLocation")
      .withRequestMessage(equalToMessage(RandomLocationRequest.newBuilder()));
  }

  @Test
  void findRandomRecoversFromNotFound() {
    this.wireMockGrpc.stubFor(
      method("GetRandomLocation")
        .willReturn(Status.NOT_FOUND, "A location was not found")
    );

    IntStream.range(0, 5)
      .forEach(i -> this.locationClient.findRandomLocation()
        .subscribe().withSubscriber(UniAssertSubscriber.create())
        .assertSubscribed()
        .awaitItem(Duration.ofSeconds(5))
        .assertItem(null)
      );

    this.wireMockGrpc.verify(5, "GetRandomLocation")
      .withRequestMessage(equalToMessage(RandomLocationRequest.newBuilder()));
  }
}
