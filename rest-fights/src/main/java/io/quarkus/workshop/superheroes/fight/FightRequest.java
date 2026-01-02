package io.quarkus.workshop.superheroes.fight;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import io.quarkus.workshop.superheroes.fight.client.Hero;
import io.quarkus.workshop.superheroes.fight.client.Villain;

@Schema(description = "A request to perform a fight between one hero and one villain in a location")
public record FightRequest(@NotNull @Valid Hero hero, @NotNull @Valid Villain villain, FightLocation location) { }
