package io.quarkus.workshop.superheroes.narration.service;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.logging.Log;

import io.quarkus.workshop.superheroes.narration.FightImage;

import dev.langchain4j.data.image.Image;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(modelName = "dalle3")
@ApplicationScoped
public interface ImageGenerationService {
  Image generateImage(String narration);

  default FightImage generateImageForNarration(String narration) {
    Log.debugf("Generating image for narration: %s", narration);
    var image = generateImage(narration);

    return new FightImage(image.url().toString(), image.revisedPrompt());
  }
}
