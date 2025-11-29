package com.wspiernik.api.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wspiernik.api.websocket.dto.OutgoingMessage;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Utility service for sending WebSocket messages.
 * Centralizes JSON serialization and error handling.
 */
@ApplicationScoped
public class MessageSender {

    private static final Logger LOG = Logger.getLogger(MessageSender.class);

    @Inject
    ObjectMapper objectMapper;

    /**
     * Send a message to a specific connection.
     */
    public void send(WebSocketConnection connection, OutgoingMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            connection.sendTextAndAwait(json);
            LOG.debugf("Sent to %s: type=%s", connection.id(), message.type());
        } catch (JsonProcessingException e) {
            LOG.errorf(e, "Failed to serialize message for %s", connection.id());
        }
    }

    /**
     * Send an error message to a connection.
     */
    public void sendError(WebSocketConnection connection, String errorMessage, String code, String requestId) {
        send(connection, OutgoingMessage.error(errorMessage, code, requestId));
    }

    /**
     * Send an error message without request ID.
     */
    public void sendError(WebSocketConnection connection, String errorMessage, String code) {
        sendError(connection, errorMessage, code, null);
    }
}
