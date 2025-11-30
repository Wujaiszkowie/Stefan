package com.wspiernik.api.websocket.handler;

import com.wspiernik.api.websocket.ConversationSessionManager;
import com.wspiernik.api.websocket.ConversationSessionManager.ConversationSession;
import com.wspiernik.api.websocket.dto.ErrorPayload;
import com.wspiernik.api.websocket.dto.IncomingMessage;
import com.wspiernik.api.websocket.dto.InterventionCompletedPayload;
import com.wspiernik.api.websocket.dto.InterventionQuestionPayload;
import com.wspiernik.api.websocket.dto.InterventionScenarioMatchedPayload;
import com.wspiernik.api.websocket.dto.OutgoingMessage;
import com.wspiernik.api.websocket.MessageSender;
import com.wspiernik.domain.intervention.InterventionService;
import com.wspiernik.domain.intervention.InterventionService.InterventionMessageResult;
import com.wspiernik.domain.intervention.InterventionService.InterventionStartResult;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Handles intervention WebSocket messages.
 * Task 18: DefaultInterventionHandler - Full implementation
 */
@ApplicationScoped
public class DefaultInterventionHandler implements InterventionHandler {

    private static final Logger LOG = Logger.getLogger(DefaultInterventionHandler.class);

    @Inject
    MessageSender messageSender;

    @Inject
    ConversationSessionManager sessionManager;

    @Inject
    InterventionService interventionService;

    @Override
    public void start(WebSocketConnection connection, IncomingMessage message) {
        LOG.info("Starting intervention for connection: " + connection.id());

        // Check if already in a session
        if (sessionManager.hasActiveSession(connection)) {
            LOG.warn("Connection already has active session");
            messageSender.sendError(connection, "Masz już aktywną sesję. Zakończ ją przed rozpoczęciem nowej.",
                    ErrorPayload.CODE_INVALID_STATE, message.requestId());
            return;
        }

        // Get scenario description from payload (optional)
        String situationDescription = message.getScenarioDescription();
        if (situationDescription == null || situationDescription.isBlank()) {
            // Fall back to text field
            situationDescription = message.getText();
        }

        // If no description provided, use generic greeting
        boolean isGenericStart = (situationDescription == null || situationDescription.isBlank());
        if (isGenericStart) {
            situationDescription = "ogólna pomoc";  // placeholder for generic intervention
        }

        try {
            // Create session
            ConversationSession session = sessionManager.startSession(connection, "intervention");

            // Start intervention via service
            InterventionStartResult result = interventionService.startIntervention(session, situationDescription);

            // Send scenario matched message if scenario was found
            if (result.scenarioMatched()) {
                InterventionScenarioMatchedPayload scenarioPayload = new InterventionScenarioMatchedPayload(
                        result.scenarioKey(),
                        result.scenarioName()
                );
                messageSender.send(connection, OutgoingMessage.of(
                        OutgoingMessage.INTERVENTION_SCENARIO_MATCHED, scenarioPayload, message.requestId()));
            }

            // Send first question
            InterventionQuestionPayload questionPayload = new InterventionQuestionPayload(
                    result.firstMessage(),
                    result.currentStep()
            );

            messageSender.send(connection, OutgoingMessage.of(
                    OutgoingMessage.INTERVENTION_QUESTION, questionPayload, message.requestId()));

            LOG.infof("Intervention started, conversation ID: %d, scenario: %s",
                    result.conversationId(), result.scenarioKey());

        } catch (Exception e) {
            LOG.errorf(e, "Failed to start intervention");
            sessionManager.endSession(connection);
            messageSender.sendError(connection, "Nie udało się rozpocząć interwencji: " + e.getMessage(),
                    ErrorPayload.CODE_INTERNAL_ERROR, message.requestId());
        }
    }

    @Override
    public void message(WebSocketConnection connection, IncomingMessage message) {
        LOG.debugf("Intervention message from %s", connection.id());

        // Check session exists
        ConversationSession session = sessionManager.getSession(connection);
        if (session == null || !"intervention".equals(session.conversationType)) {
            messageSender.sendError(connection, "Nie masz aktywnej interwencji. Rozpocznij nową interwencję.",
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
            InterventionMessageResult result = interventionService.processMessage(session, userMessage);

            if (result.completed()) {
                // Intervention completed - send completion message
                InterventionCompletedPayload completedPayload = new InterventionCompletedPayload(
                        session.conversationId,
                        "pending" // Facts extraction will be done asynchronously
                );

                // Send final response first
                InterventionQuestionPayload questionPayload = new InterventionQuestionPayload(
                        result.response(),
                        result.currentStep()
                );
                messageSender.send(connection, OutgoingMessage.of(
                        OutgoingMessage.INTERVENTION_QUESTION, questionPayload, message.requestId()));

                // Then send completion
                messageSender.send(connection, OutgoingMessage.of(
                        OutgoingMessage.INTERVENTION_COMPLETED, completedPayload, message.requestId()));

                // End session
                sessionManager.endSession(connection);
                LOG.info("Intervention completed for connection: " + connection.id());

            } else {
                // Send next question
                InterventionQuestionPayload questionPayload = new InterventionQuestionPayload(
                        result.response(),
                        result.currentStep()
                );

                messageSender.send(connection, OutgoingMessage.of(
                        OutgoingMessage.INTERVENTION_QUESTION, questionPayload, message.requestId()));
            }

        } catch (Exception e) {
            LOG.errorf(e, "Failed to process intervention message");
            messageSender.sendError(connection, "Błąd przetwarzania wiadomości: " + e.getMessage(),
                    ErrorPayload.CODE_INTERNAL_ERROR, message.requestId());
        }
    }

    @Override
    public void complete(WebSocketConnection connection, IncomingMessage message) {
        LOG.info("Manual intervention complete request from: " + connection.id());

        // Check session exists
        ConversationSession session = sessionManager.getSession(connection);
        if (session == null || !"intervention".equals(session.conversationType)) {
            messageSender.sendError(connection, "Nie masz aktywnej interwencji do zakończenia",
                    ErrorPayload.CODE_INVALID_STATE, message.requestId());
            return;
        }

        // End session (user requested early termination)
        interventionService.completeIntervention(session);
        sessionManager.endSession(connection);


        // Send completion acknowledgment
        InterventionCompletedPayload payload = new InterventionCompletedPayload(
                session.conversationId,
                "pending"
        );
        messageSender.send(connection, OutgoingMessage.of(
                OutgoingMessage.INTERVENTION_COMPLETED, payload, message.requestId()));

        LOG.info("Intervention manually completed for connection: " + connection.id());
    }
}
