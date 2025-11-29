package com.wspiernik.domain.facts;

import com.wspiernik.api.websocket.WspiernikSocket;
import com.wspiernik.api.websocket.dto.OutgoingMessage;
import com.wspiernik.domain.events.ConversationCompletedEvent;
import com.wspiernik.domain.events.FactsExtractedEvent;
import com.wspiernik.infrastructure.persistence.entity.Fact;
import com.wspiernik.infrastructure.persistence.repository.ConversationRepository;
import com.wspiernik.infrastructure.persistence.repository.FactRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for asynchronous extraction and persistence of facts from conversations.
 * Tasks 23-25: Facts Distiller
 */
@ApplicationScoped
public class FactsDistillerService {

    private static final Logger LOG = Logger.getLogger(FactsDistillerService.class);

    @Inject
    FactsExtractor factsExtractor;

    @Inject
    FactRepository factRepository;

    @Inject
    ConversationRepository conversationRepository;

    @Inject
    WspiernikSocket wspiernikSocket;

    @Inject
    Event<FactsExtractedEvent> factsExtractedEvent;

    /**
     * Async event handler for conversation completion.
     * Triggers facts extraction, persistence, and client notification.
     */
    public void onConversationCompleted(@ObservesAsync ConversationCompletedEvent event) {
        LOG.infof("Facts Distiller processing conversation %d (%s)",
                event.conversationId(), event.conversationType());

        try {
            // Get existing facts to avoid duplicates
            List<Fact> existingFacts = QuarkusTransaction.requiringNew().call(() ->
                    factRepository.findAllFacts()
            );

            // Extract facts from transcript
            List<ExtractedFact> extracted = factsExtractor.extractFacts(
                    event.rawTranscript(),
                    existingFacts
            );

            // Save extracted facts
            List<Fact> savedFacts = saveFacts(event.conversationId(), extracted);

            // Mark conversation as processed
            markConversationProcessed(event.conversationId());

            // Notify client via WebSocket
            notifyClient(event.webSocketConnectionId(), event.conversationId(), savedFacts);

            // Fire internal event
            factsExtractedEvent.fireAsync(new FactsExtractedEvent(
                    event.conversationId(),
                    savedFacts.size(),
                    savedFacts,
                    event.webSocketConnectionId()
            ));

            LOG.infof("Facts Distiller completed for conversation %d: %d facts saved",
                    event.conversationId(), savedFacts.size());

        } catch (Exception e) {
            LOG.errorf(e, "Facts Distiller failed for conversation %d", event.conversationId());
            // Don't fail silently - notify client of error
            notifyError(event.webSocketConnectionId(), event.conversationId(), e.getMessage());
        }
    }

    /**
     * Manually trigger facts extraction for a conversation.
     */
    public FactsExtractionResult extractFacts(Long conversationId, String transcript, String connectionId) {
        LOG.infof("Manual facts extraction for conversation %d", conversationId);

        try {
            // Get existing facts
            List<Fact> existingFacts = QuarkusTransaction.requiringNew().call(() ->
                    factRepository.findAllFacts()
            );

            // Extract facts
            List<ExtractedFact> extracted = factsExtractor.extractFacts(transcript, existingFacts);

            // Save facts
            List<Fact> savedFacts = saveFacts(conversationId, extracted);

            // Mark conversation processed
            markConversationProcessed(conversationId);

            return new FactsExtractionResult(true, savedFacts.size(), savedFacts, null);

        } catch (Exception e) {
            LOG.errorf(e, "Manual facts extraction failed for conversation %d", conversationId);
            return new FactsExtractionResult(false, 0, List.of(), e.getMessage());
        }
    }

    /**
     * Save extracted facts to database.
     */
    private List<Fact> saveFacts(Long conversationId, List<ExtractedFact> extracted) {
        if (extracted.isEmpty()) {
            LOG.debug("No facts to save");
            return List.of();
        }

        return QuarkusTransaction.requiringNew().call(() -> {
            List<Fact> saved = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            for (ExtractedFact ef : extracted) {
                Fact fact = new Fact();
                fact.conversationId = conversationId;
                fact.factType = ef.type();
                fact.factValue = ef.value();
                fact.severity = ef.severity();
                fact.context = ef.context();
                fact.extractedAt = now;
                fact.createdAt = now;
                factRepository.persist(fact);
                saved.add(fact);
                LOG.debugf("Saved fact: [%s] %s", ef.type(), ef.value());
            }

            return saved;
        });
    }

    /**
     * Mark conversation as having facts extracted.
     */
    private void markConversationProcessed(Long conversationId) {
        QuarkusTransaction.requiringNew().run(() -> {
            conversationRepository.markFactsExtracted(conversationId);
        });
    }

    /**
     * Notify client via WebSocket that facts have been extracted.
     */
    private void notifyClient(String connectionId, Long conversationId, List<Fact> facts) {
        if (connectionId == null) {
            LOG.debug("No connection ID provided, skipping WebSocket notification");
            return;
        }

        WebSocketConnection connection = wspiernikSocket.getConnection(connectionId);
        if (connection == null) {
            LOG.warnf("Connection %s not found, cannot notify about facts extraction", connectionId);
            return;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("conversation_id", conversationId);
            payload.put("facts_count", facts.size());
            payload.put("facts", facts.stream().map(this::factToMap).toList());

            OutgoingMessage message = new OutgoingMessage("facts_extracted", payload, null);
            wspiernikSocket.send(connection, message);
            LOG.debugf("Sent facts_extracted notification to %s", connectionId);

        } catch (Exception e) {
            LOG.warnf("Failed to send facts_extracted notification: %s", e.getMessage());
        }
    }

    /**
     * Notify client of extraction error.
     */
    private void notifyError(String connectionId, Long conversationId, String errorMessage) {
        if (connectionId == null) {
            return;
        }

        WebSocketConnection connection = wspiernikSocket.getConnection(connectionId);
        if (connection == null) {
            return;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("conversation_id", conversationId);
            payload.put("error", "Facts extraction failed: " + errorMessage);
            payload.put("facts_count", 0);

            OutgoingMessage message = new OutgoingMessage("facts_extraction_error", payload, null);
            wspiernikSocket.send(connection, message);

        } catch (Exception e) {
            LOG.warnf("Failed to send error notification: %s", e.getMessage());
        }
    }

    /**
     * Convert Fact entity to Map for JSON serialization.
     */
    private Map<String, Object> factToMap(Fact fact) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", fact.id);
        map.put("type", fact.factType);
        map.put("value", fact.factValue);
        if (fact.severity != null) {
            map.put("severity", fact.severity);
        }
        if (fact.context != null && !fact.context.isBlank()) {
            map.put("context", fact.context);
        }
        return map;
    }

    /**
     * Result of facts extraction operation.
     */
    public record FactsExtractionResult(
            boolean success,
            int factsCount,
            List<Fact> facts,
            String errorMessage
    ) {}
}
