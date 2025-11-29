package com.wspiernik.api.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload for support_completed messages.
 */
public record SupportCompletedPayload(
        @JsonProperty("conversation_id") Long conversationId,
        @JsonProperty("facts_extraction") String factsExtraction // "pending" or "completed"
) {}
