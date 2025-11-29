package com.wspiernik.domain.events;

/**
 * Event fired when a conversation (survey, intervention, or support) is completed.
 * Triggers asynchronous facts extraction.
 */
public record ConversationCompletedEvent(
        Long conversationId,
        String caregiverId,
        String conversationType,
        String rawTranscript,
        String webSocketConnectionId  // For async notification back to client
) {}
