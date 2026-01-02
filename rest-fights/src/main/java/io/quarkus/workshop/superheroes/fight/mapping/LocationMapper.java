package io.quarkus.workshop.superheroes.fight.mapping;

import static org.mapstruct.MappingConstants.ComponentModel.JAKARTA_CDI;

import org.mapstruct.Mapper;

import io.quarkus.workshop.superheroes.fight.FightLocation;

@Mapper(componentModel = JAKARTA_CDI)
public interface LocationMapper {
  FightLocation fromGrpc(io.quarkus.workshop.superheroes.location.grpc.Location grpcLocation);
}
