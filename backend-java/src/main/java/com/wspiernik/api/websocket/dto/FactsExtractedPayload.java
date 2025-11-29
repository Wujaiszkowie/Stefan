package com.wspiernik.api.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Payload for facts_extracted async notification messages.
 */
public record FactsExtractedPayload(
        @JsonProperty("conversation_id") Long conversationId,
        @JsonProperty("facts_count") int factsCount,
        @JsonProperty("facts") List<FactDto> facts
) {}
