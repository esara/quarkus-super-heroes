package io.quarkus.workshop.superheroes.fight.client;

public record NarrationImageGenerationRequest(String narration, String winnerPictureUrl, String loserPictureUrl) {
}
