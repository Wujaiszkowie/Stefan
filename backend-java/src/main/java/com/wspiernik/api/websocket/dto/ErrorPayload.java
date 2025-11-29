package com.wspiernik.api.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload for error messages.
 */
public record ErrorPayload(
        @JsonProperty("message") String message,
        @JsonProperty("code") String code
) {
    public static final String CODE_UNKNOWN_TYPE = "UNKNOWN_MESSAGE_TYPE";
    public static final String CODE_PARSE_ERROR = "MESSAGE_PARSE_ERROR";
    public static final String CODE_NO_ACTIVE_SESSION = "NO_ACTIVE_SESSION";
    public static final String CODE_SESSION_EXISTS = "SESSION_ALREADY_EXISTS";
    public static final String CODE_LLM_ERROR = "LLM_ERROR";
    public static final String CODE_DATABASE_ERROR = "DATABASE_ERROR";
    public static final String CODE_INTERNAL_ERROR = "INTERNAL_ERROR";
}
