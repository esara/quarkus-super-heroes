package io.quarkus.workshop.superheroes.narration.rest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

import io.quarkus.workshop.superheroes.narration.rest.AnthropicNarrationResourceIT.WiremockAnthropicTestProfile;

import io.quarkiverse.wiremock.devservice.WireMockConfigKey;

@QuarkusIntegrationTest
@TestProfile(WiremockAnthropicTestProfile.class)
@EnabledIf(value = "anthropicEnabled", disabledReason = "Anthropic profile is not enabled")
class AnthropicNarrationResourceIT extends NarrationResourceIT {
  private static final String NARRATION_REQUEST_JSON = """
    {
      "model": "claude-3-5-sonnet-20241022",
      "system": [
        {
          "type": "text",
          "text": "You are a marvel comics writer, expert in all sorts of super heroes and super villains."
        }
      ],
      "messages": [
        {
          "role": "user",
          "content": [
            {
              "type": "text",
              "text": "Narrate the fight between a super hero and a super villain.\\n\\nDuring the narration, don't repeat \\"super hero\\" or \\"super villain\\".\\n\\nWrite 4 paragraphs maximum. Be creative.\\n\\nThe narration must be:\\n- G rated\\n- Workplace/family safe\\n- No sexism, racism, or other bias/bigotry\\n\\nHere is the data you will use for the winner:\\n\\n+++++\\nName: %s\\nPowers: %s\\nLevel: %d\\n+++++\\n\\nHere is the data you will use for the loser:\\n\\n+++++\\nName: %s\\nPowers: %s\\nLevel: %d\\n+++++\\n\\nHere is the data you will use for the fight:\\n\\n+++++\\n%s who is a %s has won the fight against %s who is a %s.\\n\\nThe fight took place in %s, which can be described as %s.\\n+++++\\n"
            }
          ]
        }
      ],
      "max_tokens": 1024,
      "temperature": 0.7,
      "top_p": 0.5,
      "top_k": 40
    }
    """.formatted(
    FIGHT.winnerName(),
    FIGHT.winnerPowers(),
    FIGHT.winnerLevel(),
    FIGHT.loserName(),
    FIGHT.loserPowers(),
    FIGHT.loserLevel(),
    FIGHT.winnerName(),
    FIGHT.winnerTeam(),
    FIGHT.loserName(),
    FIGHT.loserTeam(),
    FIGHT.location().name(),
    FIGHT.location().description()
  );

  @Test
  @Override
  void shouldNarrateAFight() {
    super.shouldNarrateAFight();

    this.wireMock.verifyThat(
      1,
      postRequestedFor(urlPathEqualTo("/v1/messages"))
        .withHeader("x-api-key", equalTo("change-me"))
        .withRequestBody(equalToJson(NARRATION_REQUEST_JSON, true, false))
    );
  }

  @Test
  @Override
  void shouldGetAFallbackOnError() {
    this.wireMock.register(
      post(urlPathEqualTo("/v1/messages"))
        .withHeader("x-api-key", equalTo("change-me"))
        .willReturn(serverError())
    );

    super.shouldGetAFallbackOnError();

    this.wireMock.verifyThat(
      3,
      postRequestedFor(urlPathEqualTo("/v1/messages"))
        .withHeader("x-api-key", equalTo("change-me"))
    );
  }

  public static class WiremockAnthropicTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      var hostname = Boolean.getBoolean("quarkus.container-image.build") ? "host.docker.internal" : "localhost";

      return Map.of(
        "quarkus.langchain4j.anthropic.enable-integration", "true",
        "quarkus.langchain4j.anthropic.log-requests", "true",
        "quarkus.langchain4j.anthropic.log-responses", "true",
        "quarkus.langchain4j.anthropic.base-url", "http://%s:${%s}/".formatted(hostname, WireMockConfigKey.PORT),
        "quarkus.langchain4j.anthropic.chat-model.max-retries", "2",
        "quarkus.langchain4j.anthropic.timeout", "3s"
      );
    }
  }
}
