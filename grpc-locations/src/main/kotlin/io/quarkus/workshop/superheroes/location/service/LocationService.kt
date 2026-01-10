package io.quarkus.workshop.superheroes.location.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import io.quarkus.workshop.superheroes.location.Location
import io.quarkus.workshop.superheroes.location.repository.LocationRepository

@ApplicationScoped
class LocationService(private val locationRepository: LocationRepository) {
  fun getRandomLocation() = this.locationRepository.findRandom()

	fun getAllLocations() = this.locationRepository.listAll()

	fun getLocationByName(name: String?) = when(name) {
		null -> null
		else -> this.locationRepository.findByName(name)
	}

	@Transactional
	fun deleteAllLocations() {
		this.locationRepository.deleteAll()
	}

	@Transactional
	fun replaceAllLocations(locations: Collection<Location>) {
		this.locationRepository.deleteAll()

		if (locations.isNotEmpty()) {
			this.locationRepository.persist(locations)
		}
	}
}
