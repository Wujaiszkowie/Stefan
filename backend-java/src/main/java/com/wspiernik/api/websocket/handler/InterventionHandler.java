package com.wspiernik.api.websocket.handler;

import com.wspiernik.api.websocket.dto.IncomingMessage;
import io.quarkus.websockets.next.WebSocketConnection;

/**
 * Handler interface for intervention-related WebSocket messages.
 * Implemented in Intervention Module (Phase 5).
 */
public interface InterventionHandler {

    /**
     * Start a new intervention session.
     */
    void start(WebSocketConnection connection, IncomingMessage message);

    /**
     * Process an intervention message (user's response).
     */
    void message(WebSocketConnection connection, IncomingMessage message);

    /**
     * Complete the intervention session.
     */
    void complete(WebSocketConnection connection, IncomingMessage message);
}
