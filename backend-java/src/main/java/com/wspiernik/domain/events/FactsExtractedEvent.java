package com.wspiernik.domain.events;

import com.wspiernik.infrastructure.persistence.entity.Fact;

import java.util.List;

/**
 * Event fired when facts have been extracted from a conversation.
 * Used for internal signaling and WebSocket notification.
 */
public record FactsExtractedEvent(
        Long conversationId,
        int factsCount,
        List<Fact> facts,
        String webSocketConnectionId
) {}
