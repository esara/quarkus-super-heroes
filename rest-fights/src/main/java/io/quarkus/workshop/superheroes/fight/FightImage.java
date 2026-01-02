package io.quarkus.workshop.superheroes.fight;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The generated image from the narration")
public record FightImage(String imageUrl, String imageNarration) {
}
