package io.quarkus.workshop.superheroes.fight;

import jakarta.persistence.Embeddable;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Location of a fight")
@Embeddable
public record FightLocation(String name, String description, String picture) {
  public FightLocation() {
    this(null, null, null);
  }
}
