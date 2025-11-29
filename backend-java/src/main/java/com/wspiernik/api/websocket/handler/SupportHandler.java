package com.wspiernik.api.websocket.handler;

import com.wspiernik.api.websocket.dto.IncomingMessage;
import io.quarkus.websockets.next.WebSocketConnection;

/**
 * Handler interface for support-related WebSocket messages.
 * Implemented in Support Module (Phase 6).
 */
public interface SupportHandler {

    /**
     * Start a new support session.
     */
    void start(WebSocketConnection connection, IncomingMessage message);

    /**
     * Process a support message (user's message).
     */
    void message(WebSocketConnection connection, IncomingMessage message);

    /**
     * Complete the support session.
     */
    void complete(WebSocketConnection connection, IncomingMessage message);
}
