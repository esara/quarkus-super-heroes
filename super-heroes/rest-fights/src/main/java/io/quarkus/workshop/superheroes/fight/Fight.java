package io.quarkus.workshop.superheroes.fight;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/**
 * Entity class for a Fight. Re-used in the API layer
 */
@Entity
@Schema(description="Each fight has a winner and a loser")
public class Fight extends PanacheEntity {

    @NotNull
    public Instant fightDate;
    @NotNull
    public String winnerName;
    @NotNull
    public int winnerLevel;
    @NotNull
    public String winnerPicture;
    @NotNull
    public String loserName;
    @NotNull
    public int loserLevel;
    @NotNull
    public String loserPicture;
    @NotNull
    public String winnerTeam;
    @NotNull
    public String loserTeam;

}
