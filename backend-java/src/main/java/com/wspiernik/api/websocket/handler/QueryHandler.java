package com.wspiernik.api.websocket.handler;

import com.wspiernik.api.websocket.dto.IncomingMessage;
import io.quarkus.websockets.next.WebSocketConnection;

/**
 * Handler interface for query-related WebSocket messages (get_facts, get_profile).
 */
public interface QueryHandler {

    /**
     * Get facts list.
     */
    void getFacts(WebSocketConnection connection, IncomingMessage message);

    /**
     * Get profile data.
     */
    void getProfile(WebSocketConnection connection, IncomingMessage message);
}
