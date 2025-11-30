package com.wspiernik.api.rest;

import com.wspiernik.domain.facts.FactRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/fact")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FactResource {

    @Inject
    private FactRepository factRepository;

    @HEAD
    public Response facts() {
        Response.ResponseBuilder builder = factRepository.hasAnyFacts()
            ? Response.ok()
            : Response.noContent();
        return addCorsHeaders(builder).build();
    }

    @OPTIONS
    public Response preflight() {
        return addCorsHeaders(Response.ok()).build();
    }

    private Response.ResponseBuilder addCorsHeaders(Response.ResponseBuilder builder) {
        return builder
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
            .header("Access-Control-Allow-Headers", "*");
    }
}
