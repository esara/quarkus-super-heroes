package io.quarkus.workshop.superheroes.hero;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * JAX-RS API endpoints with <code>/api/heroes</code> as the base URI for all endpoints
 */
@Path("/api/heroes")
@Tag(name = "heroes")
public class HeroResource {

    Logger logger;

    @Inject
    MeterRegistry registry;

    public HeroResource(Logger logger) {
        this.logger = logger;
    }

    @Operation(summary = "Returns a random hero")
    @GET
    @Path("/random")
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Hero.class, required = true)))
    public Uni<Response> getRandomHero() {
        registry.counter("super_heroes_hero_counter", Tags.of("request", "getRandomHero")).increment();
        return Hero.findRandom()
            .onItem().ifNotNull().transform(h -> {
                this.logger.debugf("Found random hero: %s", h);
                return Response.ok(h).build();
            })
            .onItem().ifNull().continueWith(() -> {
                this.logger.debug("No random villain found");
                return Response.status(Response.Status.NOT_FOUND).build();
            });
    }

    @Operation(summary = "Returns all the heroes from the database")
    @GET
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Hero.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No Heroes")
    public Uni<List<Hero>> getAllHeroes() {
        registry.counter("super_heroes_hero_counter", Tags.of("request", "getAllHeroes")).increment();
        return Hero.listAll();
    }

    @Operation(summary = "Returns a hero for a given identifier")
    @GET
    @Path("/{id}")
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Hero.class)))
    @APIResponse(responseCode = "204", description = "The hero is not found for a given identifier")
    public Uni<RestResponse<Hero>> getHero(@RestPath Long id) {
        return Hero.<Hero>findById(id)
            .map(hero -> {
                if (hero != null) {
                    registry.counter("super_heroes_hero_counter", Tags.of("request", "getHero")).increment();
                    return RestResponse.ok(hero);
                }
                logger.debugf("No Hero found with id %d", id);
                return RestResponse.noContent();
            });
    }

    @Operation(summary = "Creates a valid hero")
    @POST
    @APIResponse(responseCode = "201", description = "The URI of the created hero", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = URI.class)))
    @WithTransaction
    public Uni<RestResponse<URI>> createHero(@Valid Hero hero, @Context UriInfo uriInfo) {
        return hero.<Hero>persist()
            .map(h -> {
                UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(Long.toString(h.id));
                logger.debug("New Hero created with URI " + builder.build().toString());
                registry.counter("super_heroes_hero_counter", Tags.of("request", "createHero")).increment();
                return RestResponse.created(builder.build());
            });
    }

    @Operation(summary = "Updates an exiting hero")
    @PUT
    @APIResponse(responseCode = "200", description = "The updated hero", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Hero.class)))
    @WithTransaction
    public Uni<Hero> updateHero(@Valid Hero hero) {
        return Hero.<Hero>findById(hero.id)
            .map(retrieved -> {
                retrieved.name = hero.name;
                retrieved.otherName = hero.otherName;
                retrieved.level = hero.level;
                retrieved.picture = hero.picture;
                retrieved.powers = hero.powers;
                registry.counter("super_heroes_hero_counter", Tags.of("request", "updateHero")).increment();
                return retrieved;
            })
            .map(h -> {
                logger.debugf("Hero updated with new valued %s", h);
                registry.counter("super_heroes_hero_counter", Tags.of("request", "updateHeroWithValue")).increment();
                return h;
            });

    }

    @Operation(summary = "Deletes an exiting hero")
    @DELETE
    @Path("/{id}")
    @APIResponse(responseCode = "204")
    @WithTransaction
    public Uni<RestResponse<Void>> deleteHero(@RestPath Long id) {
        registry.counter("super_heroes_hero_counter", Tags.of("request", "deleteHero")).increment();
        return Hero.deleteById(id)
            .invoke(() -> logger.debugf("Hero deleted with %d", id))
            .replaceWith(RestResponse.noContent());
    }

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    @Tag(name = "hello")
    public String hello() {
        registry.counter("super_heroes_hero_counter", Tags.of("request", "helloHero")).increment();
        return "Hello Hero Resource";
    }
}
