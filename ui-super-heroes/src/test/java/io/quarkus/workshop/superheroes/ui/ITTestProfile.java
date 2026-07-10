package io.quarkus.workshop.superheroes.ui;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import io.quarkiverse.quinoa.testing.QuinoaTestProfiles;

public class ITTestProfile extends QuinoaTestProfiles.Enable {
	@Override
	public Map<String, String> getConfigOverrides() {
		var configOverrides = new HashMap<>(super.getConfigOverrides());
		var fightsOpenApi = Path.of("..", "rest-fights", "src", "main", "resources", "openapi", "openapi.yml")
			.toAbsolutePath()
			.normalize()
			.toString();

		configOverrides.put("quarkus.microcks.devservices.artifacts.primaries", fightsOpenApi);
		configOverrides.put("api.base.url", "${quarkus.microcks.default.http}/rest/Fights+API/1.0");

		return configOverrides;
	}
}
