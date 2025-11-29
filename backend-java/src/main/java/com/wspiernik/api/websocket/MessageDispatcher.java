package com.wspiernik.api.websocket;

import com.wspiernik.api.websocket.dto.ErrorPayload;
import com.wspiernik.api.websocket.dto.IncomingMessage;
import com.wspiernik.api.websocket.handler.InterventionHandler;
import com.wspiernik.api.websocket.handler.QueryHandler;
import com.wspiernik.api.websocket.handler.SupportHandler;
import com.wspiernik.api.websocket.handler.SurveyHandler;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Routes incoming WebSocket messages to appropriate handlers.
 */
@ApplicationScoped
public class MessageDispatcher {

    private static final Logger LOG = Logger.getLogger(MessageDispatcher.class);

    @Inject
    SurveyHandler surveyHandler;

    @Inject
    InterventionHandler interventionHandler;

    @Inject
    SupportHandler supportHandler;

    @Inject
    QueryHandler queryHandler;

    @Inject
    MessageSender messageSender;

    @Inject
    ConversationSessionManager sessionManager;

    /**
     * Dispatch incoming message to the appropriate handler.
     */
    public void dispatch(IncomingMessage message, WebSocketConnection connection) {
        String type = message.type();
        LOG.debugf("Dispatching message type: %s from %s", type, connection.id());

        if (type == null || type.isBlank()) {
            messageSender.sendError(connection, "Message type is required",
                    ErrorPayload.CODE_PARSE_ERROR, message.requestId());
            return;
        }

        switch (type) {
            // Survey messages
            case IncomingMessage.SURVEY_START -> surveyHandler.start(connection, message);
            case IncomingMessage.SURVEY_MESSAGE -> surveyHandler.message(connection, message);
            case IncomingMessage.SURVEY_COMPLETE -> surveyHandler.complete(connection, message);

            // Intervention messages
            case IncomingMessage.INTERVENTION_START -> interventionHandler.start(connection, message);
            case IncomingMessage.INTERVENTION_MESSAGE -> interventionHandler.message(connection, message);
            case IncomingMessage.INTERVENTION_COMPLETE -> interventionHandler.complete(connection, message);

            // Support messages
            case IncomingMessage.SUPPORT_START -> supportHandler.start(connection, message);
            case IncomingMessage.SUPPORT_MESSAGE -> supportHandler.message(connection, message);
            case IncomingMessage.SUPPORT_COMPLETE -> supportHandler.complete(connection, message);

            // Query messages
            case IncomingMessage.GET_FACTS -> queryHandler.getFacts(connection, message);

            // Unknown type
            default -> {
                LOG.warnf("Unknown message type: %s from %s", type, connection.id());
                messageSender.sendError(connection, "Unknown message type: " + type,
                        ErrorPayload.CODE_UNKNOWN_TYPE, message.requestId());
            }
        }
    }

    /**
     * Called when a WebSocket connection is closed.
     * Clean up any session state.
     */
    public void onConnectionClosed(WebSocketConnection connection) {
        sessionManager.endSession(connection);
    }
}
