package com.wspiernik.api.websocket.handler;

import com.wspiernik.api.websocket.dto.IncomingMessage;
import io.quarkus.websockets.next.WebSocketConnection;

/**
 * Handler interface for survey-related WebSocket messages.
 * Implemented in Survey Module (Phase 4).
 */
public interface SurveyHandler {

    /**
     * Start a new survey session.
     */
    void start(WebSocketConnection connection, IncomingMessage message);

    /**
     * Process a survey message (user's answer).
     */
    void message(WebSocketConnection connection, IncomingMessage message);

    /**
     * Complete the survey session.
     */
    void complete(WebSocketConnection connection, IncomingMessage message);
}
