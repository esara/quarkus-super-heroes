package io.quarkus.workshop.superheroes.fight.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;

import io.quarkus.workshop.superheroes.fight.Fight;

@Mapper(componentModel = ComponentModel.JAKARTA_CDI)
public interface FightMapper {
  /**
   * Maps all fields from {@code fight} to a {@link io.quarkus.sample.superheroes.fight.schema.Fight}
   * @param fight
   * @return
   */
  io.quarkus.sample.superheroes.fight.schema.Fight toSchema(Fight fight);

  default String toString(Long id) {
    return (id != null) ? id.toString() : null;
  }
}
