package io.quarkus.workshop.superheroes.fight;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

import io.quarkus.workshop.superheroes.fight.repository.FightRepository;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerConsumerVersionSelectors;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.junitsupport.loader.SelectorBuilder;
import io.smallrye.mutiny.Uni;

@QuarkusTest
@Provider("rest-fights")
@PactFolder("pacts")
// You could comment out the @PactFolder annotation
// if you'd like to use a Pact broker. You'd also un-comment the following 2 annotations
//@PactBroker(url = "https://quarkus-super-heroes.pactflow.io")
//@EnabledIfSystemProperty(named = "pactbroker.auth.token", matches = ".+", disabledReason = "pactbroker.auth.token system property not set")
public class ContractVerificationTests {
  private static final String NO_FIGHTS_FOUND_STATE = "No fights exist";

  @ConfigProperty(name = "quarkus.http.test-port")
  int quarkusPort;

  @InjectMock
  FightRepository fightRepository;

  @TestTemplate
  @ExtendWith(PactVerificationInvocationContextProvider.class)
  void pactVerificationTestTemplate(PactVerificationContext context) {
    context.verifyInteraction();
  }

  @BeforeEach
  void beforeEach(PactVerificationContext context) {
    context.setTarget(new HttpTestTarget("localhost", this.quarkusPort));

    // Have to do this here because the CDI context doesn't seem to be available
    // in the @State method below
    var isNoFightsFoundState = Optional.ofNullable(context.getInteraction().getProviderStates())
      .orElseGet(List::of)
      .stream()
      .filter(state -> NO_FIGHTS_FOUND_STATE.equals(state.getName()))
      .count() > 0;

    if (isNoFightsFoundState) {
      when(this.fightRepository.listAll())
        .thenReturn(Uni.createFrom().item(List.of()));
    } else {
      // For other states, return a fight with an ID set
      var fight = new Fight();
      fight.setId(1L);
      fight.fightDate = java.time.Instant.now();
      fight.winnerName = "Super Baguette";
      fight.winnerLevel = 42;
      fight.winnerPowers = "eats baguette really quickly";
      fight.winnerPicture = "super_baguette.png";
      fight.loserName = "Super Chocolatine";
      fight.loserLevel = 40;
      fight.loserPowers = "does not eat pain au chocolat";
      fight.loserPicture = "super_chocolatine.png";
      fight.winnerTeam = "heroes";
      fight.loserTeam = "villains";
      fight.location = new FightLocation("Gotham City", "An American city rife with corruption and crime, the home of its iconic protector Batman.", "https://somewhere.com/gotham_city.png");
      
      when(this.fightRepository.listAll())
        .thenReturn(Uni.createFrom().item(List.of(fight)));
      
      when(this.fightRepository.persist(any(Fight.class)))
        .thenAnswer(invocation -> {
          Fight f = invocation.getArgument(0);
          if (f.getId() == null) {
            f.setId(1L);
          }
          return Uni.createFrom().item(f);
        });
    }
  }

  @PactBrokerConsumerVersionSelectors
  public static SelectorBuilder consumerVersionSelectors() {
    return new SelectorBuilder()
      .branch(System.getProperty("pactbroker.consumer.branch", "main"));
  }

  @State(NO_FIGHTS_FOUND_STATE)
  public void clearData() {
    // Already handled in beforeEach
  }
}
