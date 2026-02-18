package io.quarkus.workshop.superheroes.fight;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Location of a fight")
@Embeddable
public record FightLocation(
	@Column(length = 500) String name,
	@Lob String description,
	@Lob String picture
) {
	public FightLocation() {
		this(null, null, null);
	}
}
