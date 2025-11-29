package com.wspiernik.api.websocket.handler;

import com.wspiernik.api.websocket.dto.ErrorPayload;
import com.wspiernik.api.websocket.dto.IncomingMessage;
import com.wspiernik.api.websocket.dto.OutgoingMessage;
import com.wspiernik.api.websocket.MessageSender;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Default (stub) implementation of SurveyHandler.
 * Will be replaced by actual implementation in Survey Module (Phase 4).
 */
@ApplicationScoped
public class DefaultSurveyHandler implements SurveyHandler {

    private static final Logger LOG = Logger.getLogger(DefaultSurveyHandler.class);

    @Inject
    MessageSender messageSender;

    @Override
    public void start(WebSocketConnection connection, IncomingMessage message) {
        LOG.info("Survey start - stub implementation");
        messageSender.sendError(connection, "Survey module not yet implemented",
                ErrorPayload.CODE_INTERNAL_ERROR, message.requestId());
    }

    @Override
    public void message(WebSocketConnection connection, IncomingMessage message) {
        LOG.info("Survey message - stub implementation");
        messageSender.sendError(connection, "Survey module not yet implemented",
                ErrorPayload.CODE_INTERNAL_ERROR, message.requestId());
    }

    @Override
    public void complete(WebSocketConnection connection, IncomingMessage message) {
        LOG.info("Survey complete - stub implementation");
        messageSender.sendError(connection, "Survey module not yet implemented",
                ErrorPayload.CODE_INTERNAL_ERROR, message.requestId());
    }
}
