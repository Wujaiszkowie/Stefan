package com.wspiernik.api.rest;

import com.wspiernik.infrastructure.llm.LlmClient;
import com.wspiernik.infrastructure.persistence.entity.CrisisScenario;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;

/**
 * Health check endpoint for monitoring and Docker healthchecks.
 * Task 28: Integration & Cleanup
 */
@Path("/api")
public class HealthResource {

    private static final Logger LOG = Logger.getLogger(HealthResource.class);

    @Inject
    LlmClient llmClient;

    /**
     * Health check endpoint.
     * Returns status of all components.
     */
    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    public HealthStatus health() {
        ComponentStatus database = checkDatabase();
        ComponentStatus llm = checkLlm();

        // Overall status is UP only if all components are UP
        String overallStatus = "UP";
        if (!"UP".equals(database.status()) || !"UP".equals(llm.status())) {
            overallStatus = "DEGRADED";
        }

        return new HealthStatus(
                overallStatus,
                LocalDateTime.now(),
                database,
                llm
        );
    }

    /**
     * Simple ping endpoint.
     */
    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        return "pong";
    }

    /**
     * Check database connectivity.
     */
    private ComponentStatus checkDatabase() {
        try {
            long count = QuarkusTransaction.requiringNew().call(() ->
                    CrisisScenario.count()
            );
            return new ComponentStatus(
                    "SQLite",
                    "UP",
                    "Connected (" + count + " scenarios)"
            );
        } catch (Exception e) {
            LOG.errorf(e, "Database health check failed");
            return new ComponentStatus(
                    "SQLite",
                    "DOWN",
                    "Error: " + e.getMessage()
            );
        }
    }

    /**
     * Check LLM connectivity.
     */
    private ComponentStatus checkLlm() {
        try {
            String response = llmClient.generate("Respond with 'ok'", "ping");
            if (response != null && !response.isEmpty()) {
                return new ComponentStatus(
                        "LLM",
                        "UP",
                        "Responding"
                );
            } else {
                return new ComponentStatus(
                        "LLM",
                        "DOWN",
                        "Empty response"
                );
            }
        } catch (Exception e) {
            LOG.warnf("LLM health check failed: %s", e.getMessage());
            return new ComponentStatus(
                    "LLM",
                    "DOWN",
                    "Error: " + e.getMessage()
            );
        }
    }

    /**
     * Health status response.
     */
    public record HealthStatus(
            String status,
            LocalDateTime timestamp,
            ComponentStatus database,
            ComponentStatus llm
    ) {}

    /**
     * Component status.
     */
    public record ComponentStatus(
            String name,
            String status,
            String message
    ) {}
}
