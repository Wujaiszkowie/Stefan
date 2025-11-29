package com.wspiernik.infrastructure.llm;

import com.wspiernik.infrastructure.llm.dto.LlmRequest;
import com.wspiernik.infrastructure.llm.dto.LlmResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST Client interface for Bielnik LLM API.
 * Uses OpenAI-compatible chat completions endpoint.
 */
@Path("/v1")
@RegisterRestClient(configKey = "bielnik-api")
public interface BielnikApi {

    /**
     * Send a chat completion request to the LLM.
     *
     * @param request The chat completion request
     * @return The LLM response with generated content
     */
    @POST
    @Path("/chat/completions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    LlmResponse chatCompletion(LlmRequest request);
}
