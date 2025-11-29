package com.wspiernik.api.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload for intervention_scenario_matched messages.
 */
public record InterventionScenarioMatchedPayload(
        @JsonProperty("scenario_key") String scenarioKey,
        @JsonProperty("scenario_name") String scenarioName
) {}
