package com.wspiernik.api.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload for support_message outgoing messages.
 */
public record SupportMessagePayload(
        @JsonProperty("text") String text
) {}
