package com.wspiernik.api.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload for intervention_question messages.
 */
public record InterventionQuestionPayload(
        @JsonProperty("question") String question,
        @JsonProperty("step") int step
) {}
