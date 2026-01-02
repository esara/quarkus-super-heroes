package io.quarkus.workshop.superheroes.fight.repository;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.hibernate.reactive.panache.common.WithSession;

import io.quarkus.workshop.superheroes.fight.Fight;

import io.smallrye.mutiny.Uni;

/**
 * Repository class for managing data operations on a {@link Fight}.
 */
@ApplicationScoped
@WithSession
public class FightRepository implements PanacheRepository<Fight> {
	public Uni<List<Fight>> listAll() {
		return findAll().list();
	}
}

