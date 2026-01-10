package io.quarkus.workshop.superheroes.villain.service;

import static jakarta.transaction.Transactional.TxType.*;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import io.quarkus.logging.Log;

import io.quarkus.workshop.superheroes.villain.Villain;
import io.quarkus.workshop.superheroes.villain.config.VillainConfig;
import io.quarkus.workshop.superheroes.villain.mapping.VillainFullUpdateMapper;
import io.quarkus.workshop.superheroes.villain.mapping.VillainPartialUpdateMapper;

/**
 * Service class containing business methods for the application.
 */
@ApplicationScoped
@Transactional(REQUIRED)
public class VillainService {
  @Inject
	Validator validator;

  @Inject
  VillainConfig villainConfig;

  @Inject
  VillainPartialUpdateMapper villainPartialUpdateMapper;

  @Inject
  VillainFullUpdateMapper villainFullUpdateMapper;

	@Transactional(SUPPORTS)
	public List<Villain> findAllVillains() {
    Log.debug("Getting all villains");
    return Optional.ofNullable(Villain.<Villain>listAll())
      .orElseGet(List::of);
	}

  @Transactional(SUPPORTS)
  public List<Villain> findAllVillainsHavingName(String name) {
    Log.debugf("Finding all villains having name = %s", name);
    return Optional.ofNullable(Villain.listAllWhereNameLike(name))
      .orElseGet(List::of);
  }

  @Transactional(SUPPORTS)
  public Optional<Villain> findVillainById(Long id) {
    Log.debugf("Finding villain by id = %d", id);
    return Villain.findByIdOptional(id);
  }

	@Transactional(SUPPORTS)
	public Optional<Villain> findRandomVillain() {
    Log.debug("Finding a random villain");
		return Villain.findRandom();
	}

	public Villain persistVillain(@NotNull @Valid Villain villain) {
    Log.debugf("Persisting villain: %s", villain);
		villain.level = (int) Math.round(villain.level * this.villainConfig.level().multiplier());
		Villain.persist(villain);

		return villain;
	}

	public Optional<Villain> replaceVillain(@NotNull @Valid Villain villain) {
    Log.debugf("Replacing villain: %s", villain);
		return Villain.findByIdOptional(villain.id)
			.map(Villain.class::cast) // Only here for type erasure within the IDE
			.map(v -> {
				this.villainFullUpdateMapper.mapFullUpdate(villain, v);
				return v;
			});
	}

	public Optional<Villain> partialUpdateVillain(@NotNull Villain villain) {
    Log.debugf("Partially updating villain: %s", villain);
		return Villain.findByIdOptional(villain.id)
			.map(Villain.class::cast) // Only here for type erasure within the IDE
			.map(v -> {
				this.villainPartialUpdateMapper.mapPartialUpdate(villain, v);
				return v;
			})
			.map(this::validatePartialUpdate);
	}

  public void replaceAllVillains(List<Villain> villains) {
    Log.debug("Replacing all villains");
    deleteAllVillains();
    Villain.persist(villains);
  }

	/**
	 * Validates a {@link Villain} for a partial update according to annotated validation rules on the {@link Villain} object.
	 * @param villain The {@link Villain}
	 * @return The same {@link Villain} that was passed in, assuming it passes validation. The return is used as a convenience so the method can be called in a functional pipeline.
	 * @throws ConstraintViolationException If validation fails
	 */
	private Villain validatePartialUpdate(Villain villain) {
		var violations = this.validator.validate(villain);

		if ((violations != null) && !violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}

		return villain;
	}

	public void deleteAllVillains() {
    Log.debug("Deleting all villains");
		List<Villain> villains = Villain.listAll();
		villains.stream()
			.map(v -> v.id)
			.forEach(this::deleteVillain);
	}

	public void deleteVillain(Long id) {
    Log.debugf("Deleting villain by id = %d", id);
		Villain.deleteById(id);
	}
}
