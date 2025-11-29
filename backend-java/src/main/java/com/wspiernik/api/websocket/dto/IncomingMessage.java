package com.wspiernik.api.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Base wrapper for all incoming WebSocket messages.
 * Format: {"type": "...", "payload": {...}, "request_id": "..."}
 */
public record IncomingMessage(
        @JsonProperty("type") String type,
        @JsonProperty("payload") JsonNode payload,
        @JsonProperty("request_id") String requestId
) {
    /**
     * Message types for incoming messages.
     */
    public static final String SURVEY_START = "survey_start";
    public static final String SURVEY_MESSAGE = "survey_message";
    public static final String SURVEY_COMPLETE = "survey_complete";
    public static final String INTERVENTION_START = "intervention_start";
    public static final String INTERVENTION_MESSAGE = "intervention_message";
    public static final String INTERVENTION_COMPLETE = "intervention_complete";
    public static final String SUPPORT_START = "support_start";
    public static final String SUPPORT_MESSAGE = "support_message";
    public static final String SUPPORT_COMPLETE = "support_complete";
    public static final String GET_FACTS = "get_facts";
    public static final String GET_PROFILE = "get_profile";

    /**
     * Extract text from payload (for message types).
     */
    public String getText() {
        if (payload == null || !payload.has("text")) {
            return null;
        }
        return payload.get("text").asText();
    }

    /**
     * Extract scenario description from payload (for intervention_start).
     */
    public String getScenarioDescription() {
        if (payload == null || !payload.has("scenario_description")) {
            return null;
        }
        return payload.get("scenario_description").asText();
    }

    /**
     * Extract limit from payload (for get_facts).
     */
    public int getLimit() {
        if (payload == null || !payload.has("limit")) {
            return 10; // default
        }
        return payload.get("limit").asInt(10);
    }
}
