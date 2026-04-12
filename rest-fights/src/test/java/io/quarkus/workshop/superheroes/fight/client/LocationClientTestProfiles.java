package io.quarkus.workshop.superheroes.fight.client;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

/**
 * Profiles for {@link LocationClient} tests. MicroProfile Fault Tolerance allows only one effective
 * {@code CircuitBreaker/requestVolumeThreshold} per method per deployment, so WireMock stub tests and the strict
 * circuit-breaker trip scenario run under different {@link QuarkusTestProfile}s (two test types).
 */
public final class LocationClientTestProfiles {

  private LocationClientTestProfiles() {
  }

  /**
   * Prevents {@link LocationClient#findRandomLocation()} from opening due to unrelated suite traffic (gRPC channel
   * noise, pact provider calls, etc.) while still exercising the real interceptor stack.
   */
  public static final class HighCircuitBreakerVolumeForFindRandomLocation implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of(
        "%s/findRandomLocation/CircuitBreaker/requestVolumeThreshold".formatted(LocationClient.class.getName()),
        "10000"
      );
    }
  }
}
