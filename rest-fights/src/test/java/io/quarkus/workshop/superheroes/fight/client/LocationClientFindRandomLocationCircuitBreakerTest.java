package io.quarkus.workshop.superheroes.fight.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.wiremock.grpc.dsl.WireMockGrpc.*;

import java.time.Duration;
import java.util.stream.IntStream;

import jakarta.inject.Inject;

import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wiremock.grpc.dsl.WireMockGrpcService;

import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

import io.quarkus.workshop.superheroes.fight.InjectGrpcWireMock;
import io.quarkus.workshop.superheroes.fight.LocationsWiremockGrpcServerResource;
import io.quarkus.workshop.superheroes.location.grpc.RandomLocationRequest;

import io.grpc.StatusRuntimeException;
import io.smallrye.faulttolerance.api.CircuitBreakerMaintenance;
import io.smallrye.faulttolerance.api.CircuitBreakerState;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;

/**
 * Asserts {@link LocationClient#findRandomLocation()} opens the circuit breaker under default MP FT limits.
 * <p>
 *   Kept separate from {@link LocationClientTests} because that class raises {@code requestVolumeThreshold} via
 *   {@link io.quarkus.test.junit.QuarkusTestProfile} so stub tests stay stable; this deployment uses the real
 *   annotation defaults from {@link LocationClient}.
 * </p>
 */
@QuarkusTest
@WithTestResource(LocationsWiremockGrpcServerResource.class)
class LocationClientFindRandomLocationCircuitBreakerTest {

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
  void findRandomDoesntRecoverFromError() {
    this.wireMockGrpc.stubFor(
      method("GetRandomLocation")
        .willReturn(Status.UNAVAILABLE, "Service isn't there")
    );

    assertThat(this.circuitBreakerMaintenance.currentState("findRandomLocation"))
      .isEqualTo(CircuitBreakerState.CLOSED);

    IntStream.rangeClosed(1, 2)
      .forEach(i ->
        this.locationClient.findRandomLocation()
          .subscribe().withSubscriber(UniAssertSubscriber.create())
          .assertSubscribed()
          .awaitFailure(Duration.ofSeconds(5))
          .assertFailedWith(StatusRuntimeException.class)
      );

    var ex = this.locationClient.findRandomLocation()
      .subscribe().withSubscriber(UniAssertSubscriber.create())
      .assertSubscribed()
      .awaitFailure(Duration.ofSeconds(5))
      .getFailure();

    assertThat(ex)
      .isNotNull()
      .isExactlyInstanceOf(CircuitBreakerOpenException.class)
      .hasMessageContainingAll("%s#findRandomLocation".formatted(LocationClient.class.getName()), "circuit breaker is open");

    assertThat(this.circuitBreakerMaintenance.currentState("findRandomLocation"))
      .isEqualTo(CircuitBreakerState.OPEN);

    this.wireMockGrpc.verify(8, "GetRandomLocation")
      .withRequestMessage(equalToMessage(RandomLocationRequest.newBuilder()));
  }
}
