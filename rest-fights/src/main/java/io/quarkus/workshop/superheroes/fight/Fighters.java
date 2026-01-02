package io.quarkus.workshop.superheroes.fight;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import io.quarkus.workshop.superheroes.fight.client.Hero;
import io.quarkus.workshop.superheroes.fight.client.Villain;

/**
 * Entity class representing Fighters
 */
@Schema(description = "A fight between one hero and one villain")
public record Fighters(@NotNull @Valid Hero hero, @NotNull @Valid Villain villain) { }
