package com.wspiernik.api.websocket.handler;

import com.wspiernik.api.websocket.ConversationSessionManager;
import com.wspiernik.api.websocket.ConversationSessionManager.ConversationSession;
import com.wspiernik.api.websocket.dto.ErrorPayload;
import com.wspiernik.api.websocket.dto.IncomingMessage;
import com.wspiernik.api.websocket.dto.OutgoingMessage;
import com.wspiernik.api.websocket.dto.SurveyCompletedPayload;
import com.wspiernik.api.websocket.dto.SurveyQuestionPayload;
import com.wspiernik.api.websocket.MessageSender;
import com.wspiernik.domain.survey.SurveyService;
import com.wspiernik.domain.survey.SurveyService.SurveyMessageResult;
import com.wspiernik.domain.survey.SurveyService.SurveyStartResult;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Handles survey WebSocket messages.
 * Routes to SurveyService for business logic.
 */
@ApplicationScoped
public class DefaultSurveyHandler implements SurveyHandler {

    private static final Logger LOG = Logger.getLogger(DefaultSurveyHandler.class);

    @Inject
    MessageSender messageSender;

    @Inject
    ConversationSessionManager sessionManager;

    @Inject
    SurveyService surveyService;

    @Override
    public void start(WebSocketConnection connection, IncomingMessage message) {
        LOG.info("Starting survey for connection: " + connection.id());

        // Check if already in a session
        if (sessionManager.hasActiveSession(connection)) {
            LOG.warn("Connection already has active session");
            messageSender.sendError(connection, "Masz już aktywną sesję. Zakończ ją przed rozpoczęciem nowej.",
                    ErrorPayload.CODE_INVALID_STATE, message.requestId());
            return;
        }

        try {
            // Create session
            ConversationSession session = sessionManager.startSession(connection, "survey");

            // Start survey via service
            SurveyStartResult result = surveyService.startSurvey(session);

            // Send first question
            SurveyQuestionPayload payload = new SurveyQuestionPayload(
                    result.question(),
                    result.currentStep().getOrder()
            );

            messageSender.send(connection, OutgoingMessage.of(
                    OutgoingMessage.SURVEY_QUESTION, payload, message.requestId()));

            LOG.infof("Survey started, conversation ID: %d", result.conversationId());

        } catch (Exception e) {
            LOG.errorf(e, "Failed to start survey");
            sessionManager.endSession(connection);
            messageSender.sendError(connection, "Nie udało się rozpocząć ankiety: " + e.getMessage(),
                    ErrorPayload.CODE_INTERNAL_ERROR, message.requestId());
        }
    }

    @Override
    public void message(WebSocketConnection connection, IncomingMessage message) {
        LOG.debugf("Survey message from %s: %s", connection.id(), message.getContent());

        //FIXME history of conversation is taken from a session, not from repository
        // Check session exists
        ConversationSession session = sessionManager.getSession(connection);
        if (session == null || !"survey".equals(session.conversationType)) {
            messageSender.sendError(connection, "Nie masz aktywnej ankiety. Rozpocznij nową ankietę.",
                    ErrorPayload.CODE_INVALID_STATE, message.requestId());
            return;
        }

        String userMessage = message.getContent();
        if (userMessage == null || userMessage.isBlank()) {
            messageSender.sendError(connection, "Wiadomość nie może być pusta",
                    ErrorPayload.CODE_VALIDATION_ERROR, message.requestId());
            return;
        }

        try {
            // Process message via service
            SurveyMessageResult result = surveyService.processMessage(session, userMessage);

            if (result.completed()) {
                // Survey completed - send completion message
                SurveyCompletedPayload payload = new SurveyCompletedPayload(
                        null, // Profile ID could be added later
                        0     // Facts saved count - will be populated by fact extraction
                );

                messageSender.send(connection, OutgoingMessage.of(
                        OutgoingMessage.SURVEY_COMPLETED, payload, message.requestId()));

                // End session
                sessionManager.endSession(connection);
                LOG.info("Survey completed for connection: " + connection.id());

            } else {
                // Send next question
                SurveyQuestionPayload payload = new SurveyQuestionPayload(
                        result.response(),
                        result.currentStep() != null ? result.currentStep().getOrder() : 0
                );

                messageSender.send(connection, OutgoingMessage.of(
                        OutgoingMessage.SURVEY_QUESTION, payload, message.requestId()));
            }

        } catch (Exception e) {
            LOG.errorf(e, "Failed to process survey message");
            messageSender.sendError(connection, "Błąd przetwarzania wiadomości: " + e.getMessage(),
                    ErrorPayload.CODE_INTERNAL_ERROR, message.requestId());
        }
    }

    @Override
    public void complete(WebSocketConnection connection, IncomingMessage message) {
        LOG.info("Manual survey complete request from: " + connection.id());

        // Check session exists
        ConversationSession session = sessionManager.getSession(connection);
        if (session == null || !"survey".equals(session.conversationType)) {
            messageSender.sendError(connection, "Nie masz aktywnej ankiety do zakończenia",
                    ErrorPayload.CODE_INVALID_STATE, message.requestId());
            return;
        }

        // End session (user requested early termination)
        sessionManager.endSession(connection);

        // Send completion acknowledgment
        SurveyCompletedPayload payload = new SurveyCompletedPayload(null, 0);
        messageSender.send(connection, OutgoingMessage.of(
                OutgoingMessage.SURVEY_COMPLETED, payload, message.requestId()));

        LOG.info("Survey manually completed for connection: " + connection.id());
    }
}
