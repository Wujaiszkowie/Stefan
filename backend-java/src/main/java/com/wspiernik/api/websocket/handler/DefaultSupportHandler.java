package com.wspiernik.api.websocket.handler;

import com.wspiernik.api.websocket.ConversationSessionManager;
import com.wspiernik.api.websocket.ConversationSessionManager.ConversationSession;
import com.wspiernik.api.websocket.dto.ErrorPayload;
import com.wspiernik.api.websocket.dto.IncomingMessage;
import com.wspiernik.api.websocket.dto.OutgoingMessage;
import com.wspiernik.api.websocket.dto.SupportCompletedPayload;
import com.wspiernik.api.websocket.dto.SupportMessagePayload;
import com.wspiernik.api.websocket.MessageSender;
import com.wspiernik.domain.support.SupportService;
import com.wspiernik.domain.support.SupportService.SupportCompleteResult;
import com.wspiernik.domain.support.SupportService.SupportMessageResult;
import com.wspiernik.domain.support.SupportService.SupportStartResult;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Handles support WebSocket messages.
 * Task 21: DefaultSupportHandler - Full implementation
 */
@ApplicationScoped
public class DefaultSupportHandler implements SupportHandler {

    private static final Logger LOG = Logger.getLogger(DefaultSupportHandler.class);

    @Inject
    MessageSender messageSender;

    @Inject
    ConversationSessionManager sessionManager;

    @Inject
    SupportService supportService;

    @Override
    public void start(WebSocketConnection connection, IncomingMessage message) {
        LOG.info("Starting support session for connection: " + connection.id());

        // Check if already in a session
        if (sessionManager.hasActiveSession(connection)) {
            LOG.warn("Connection already has active session");
            messageSender.sendError(connection, "Masz już aktywną sesję. Zakończ ją przed rozpoczęciem nowej.",
                    ErrorPayload.CODE_INVALID_STATE, message.requestId());
            return;
        }

        try {
            // Create session
            ConversationSession session = sessionManager.startSession(connection, "support");

            // Get initial message if provided
            String initialMessage = message.getText();

            // Start support via service
            SupportStartResult result = supportService.startSupport(session, initialMessage);

            // Send greeting/response
            SupportMessagePayload payload = new SupportMessagePayload(result.greeting());

            messageSender.send(connection, OutgoingMessage.of(
                    OutgoingMessage.SUPPORT_MESSAGE, payload, message.requestId()));

            LOG.infof("Support session started, conversation ID: %d", result.conversationId());

        } catch (Exception e) {
            LOG.errorf(e, "Failed to start support session");
            sessionManager.endSession(connection);
            messageSender.sendError(connection, "Nie udało się rozpocząć sesji wsparcia: " + e.getMessage(),
                    ErrorPayload.CODE_INTERNAL_ERROR, message.requestId());
        }
    }

    @Override
    public void message(WebSocketConnection connection, IncomingMessage message) {
        LOG.debugf("Support message from %s", connection.id());

        // Check session exists
        ConversationSession session = sessionManager.getSession(connection);
        if (session == null || !"support".equals(session.conversationType)) {
            messageSender.sendError(connection, "Nie masz aktywnej sesji wsparcia. Rozpocznij nową sesję.",
                    ErrorPayload.CODE_INVALID_STATE, message.requestId());
            return;
        }

        String userMessage = message.getText();
        if (userMessage == null || userMessage.isBlank()) {
            messageSender.sendError(connection, "Wiadomość nie może być pusta",
                    ErrorPayload.CODE_VALIDATION_ERROR, message.requestId());
            return;
        }

        try {
            // Process message via service
            SupportMessageResult result = supportService.processMessage(session, userMessage);

            // Send response
            SupportMessagePayload payload = new SupportMessagePayload(result.response());

            messageSender.send(connection, OutgoingMessage.of(
                    OutgoingMessage.SUPPORT_MESSAGE, payload, message.requestId()));

            if (result.completed()) {
                // Support completed - send completion message
                SupportCompletedPayload completedPayload = new SupportCompletedPayload(
                        session.conversationId,
                        "pending"
                );
                supportService.completeIntervention(session);
                messageSender.send(connection, OutgoingMessage.of(
                        OutgoingMessage.SUPPORT_COMPLETED, completedPayload, message.requestId()));

                // End session
                sessionManager.endSession(connection);
                LOG.info("Support session completed for connection: " + connection.id());
            }

        } catch (Exception e) {
            LOG.errorf(e, "Failed to process support message");
            messageSender.sendError(connection, "Błąd przetwarzania wiadomości: " + e.getMessage(),
                    ErrorPayload.CODE_INTERNAL_ERROR, message.requestId());
        }
    }

    @Override
    public void complete(WebSocketConnection connection, IncomingMessage message) {
        LOG.info("Support complete request from: " + connection.id());

        // Check session exists
        ConversationSession session = sessionManager.getSession(connection);
        if (session == null || !"support".equals(session.conversationType)) {
            messageSender.sendError(connection, "Nie masz aktywnej sesji wsparcia do zakończenia",
                    ErrorPayload.CODE_INVALID_STATE, message.requestId());
            return;
        }

        try {
            // Complete support via service
            SupportCompleteResult result = supportService.completeSupport(session);

            // Send farewell message
            SupportMessagePayload farewellPayload = new SupportMessagePayload(result.farewell());
            messageSender.send(connection, OutgoingMessage.of(
                    OutgoingMessage.SUPPORT_MESSAGE, farewellPayload, message.requestId()));

            // Send completion acknowledgment
            SupportCompletedPayload completedPayload = new SupportCompletedPayload(
                    result.conversationId(),
                    "pending"
            );
            messageSender.send(connection, OutgoingMessage.of(
                    OutgoingMessage.SUPPORT_COMPLETED, completedPayload, message.requestId()));

            // End session
            sessionManager.endSession(connection);
            LOG.info("Support session completed for connection: " + connection.id());

        } catch (Exception e) {
            LOG.errorf(e, "Failed to complete support session");
            messageSender.sendError(connection, "Błąd zakończenia sesji: " + e.getMessage(),
                    ErrorPayload.CODE_INTERNAL_ERROR, message.requestId());
        }
    }
}
