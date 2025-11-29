package com.wspiernik.api.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wspiernik.api.websocket.dto.ErrorPayload;
import com.wspiernik.api.websocket.dto.IncomingMessage;
import com.wspiernik.api.websocket.dto.OutgoingMessage;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnError;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main WebSocket endpoint for Wspiernik application.
 * Handles all real-time communication between frontend and backend.
 */
@WebSocket(path = "/ws")
public class WspiernikSocket {

    private static final Logger LOG = Logger.getLogger(WspiernikSocket.class);

    /**
     * Registry of active connections for broadcast capability.
     */
    private static final Map<String, WebSocketConnection> connections = new ConcurrentHashMap<>();

    @Inject
    ObjectMapper objectMapper;

    @Inject
    MessageDispatcher messageDispatcher;

    @OnOpen
    public void onOpen(WebSocketConnection connection) {
        String connectionId = connection.id();
        connections.put(connectionId, connection);
        LOG.infof("WebSocket opened: %s (total connections: %d)", connectionId, connections.size());
    }

    @OnTextMessage
    public void onMessage(String message, WebSocketConnection connection) {
        String connectionId = connection.id();
        LOG.debugf("WebSocket message from %s: %s", connectionId, message);

        try {
            IncomingMessage incomingMessage = objectMapper.readValue(message, IncomingMessage.class);
            messageDispatcher.dispatch(incomingMessage, connection);
        } catch (JsonProcessingException e) {
            LOG.warnf("Failed to parse message from %s: %s", connectionId, e.getMessage());
            sendError(connection, "Invalid JSON format: " + e.getMessage(), ErrorPayload.CODE_PARSE_ERROR, null);
        } catch (Exception e) {
            LOG.errorf(e, "Error processing message from %s", connectionId);
            sendError(connection, "Internal error: " + e.getMessage(), ErrorPayload.CODE_INTERNAL_ERROR, null);
        }
    }

    @OnClose
    public void onClose(WebSocketConnection connection) {
        String connectionId = connection.id();
        connections.remove(connectionId);
        messageDispatcher.onConnectionClosed(connection);
        LOG.infof("WebSocket closed: %s (remaining connections: %d)", connectionId, connections.size());
    }

    @OnError
    public void onError(WebSocketConnection connection, Throwable error) {
        String connectionId = connection.id();
        LOG.errorf(error, "WebSocket error for %s", connectionId);
        connections.remove(connectionId);
        messageDispatcher.onConnectionClosed(connection);
    }

    /**
     * Send a message to a specific connection.
     */
    public void send(WebSocketConnection connection, OutgoingMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            connection.sendTextAndAwait(json);
            LOG.debugf("Sent to %s: %s", connection.id(), message.type());
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
     * Broadcast a message to all connected clients.
     */
    public void broadcast(OutgoingMessage message) {
        connections.values().forEach(conn -> send(conn, message));
    }

    /**
     * Get a connection by ID.
     */
    public WebSocketConnection getConnection(String connectionId) {
        return connections.get(connectionId);
    }

    /**
     * Get the number of active connections.
     */
    public int getConnectionCount() {
        return connections.size();
    }
}
