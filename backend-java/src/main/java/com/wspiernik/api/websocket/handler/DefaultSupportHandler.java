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
 * Default (stub) implementation of SupportHandler.
 * Will be replaced by actual implementation in Support Module (Phase 6).
 */
@ApplicationScoped
public class DefaultSupportHandler implements SupportHandler {

    private static final Logger LOG = Logger.getLogger(DefaultSupportHandler.class);

    @Inject
    MessageSender messageSender;

    @Override
    public void start(WebSocketConnection connection, IncomingMessage message) {
        LOG.info("Support start - stub implementation");
        messageSender.sendError(connection, "Support module not yet implemented",
                ErrorPayload.CODE_INTERNAL_ERROR, message.requestId());
    }

    @Override
    public void message(WebSocketConnection connection, IncomingMessage message) {
        LOG.info("Support message - stub implementation");
        messageSender.sendError(connection, "Support module not yet implemented",
                ErrorPayload.CODE_INTERNAL_ERROR, message.requestId());
    }

    @Override
    public void complete(WebSocketConnection connection, IncomingMessage message) {
        LOG.info("Support complete - stub implementation");
        messageSender.sendError(connection, "Support module not yet implemented",
                ErrorPayload.CODE_INTERNAL_ERROR, message.requestId());
    }
}
