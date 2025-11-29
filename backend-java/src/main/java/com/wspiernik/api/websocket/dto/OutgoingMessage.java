package com.wspiernik.api.websocket.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Base wrapper for all outgoing WebSocket messages.
 * Format: {"type": "...", "payload": {...}, "request_id": "..."}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OutgoingMessage(
        @JsonProperty("type") String type,
        @JsonProperty("payload") Object payload,
        @JsonProperty("request_id") String requestId
) {
    /**
     * Message types for outgoing messages.
     */
    public static final String SURVEY_QUESTION = "survey_question";
    public static final String SURVEY_COMPLETED = "survey_completed";
    public static final String INTERVENTION_SCENARIO_MATCHED = "intervention_scenario_matched";
    public static final String INTERVENTION_QUESTION = "intervention_question";
    public static final String INTERVENTION_COMPLETED = "intervention_completed";
    public static final String SUPPORT_MESSAGE = "support_message";
    public static final String SUPPORT_COMPLETED = "support_completed";
    public static final String FACTS_EXTRACTED = "facts_extracted";
    public static final String FACTS_LIST = "facts_list";
    public static final String PROFILE_DATA = "profile_data";
    public static final String ERROR = "error";

    /**
     * Create an outgoing message with type and payload.
     */
    public static OutgoingMessage of(String type, Object payload) {
        return new OutgoingMessage(type, payload, null);
    }

    /**
     * Create an outgoing message with type, payload, and request ID.
     */
    public static OutgoingMessage of(String type, Object payload, String requestId) {
        return new OutgoingMessage(type, payload, requestId);
    }

    /**
     * Create an error message.
     */
    public static OutgoingMessage error(String message, String code, String requestId) {
        return new OutgoingMessage(ERROR, new ErrorPayload(message, code), requestId);
    }

    /**
     * Create an error message without request ID.
     */
    public static OutgoingMessage error(String message, String code) {
        return error(message, code, null);
    }
}
