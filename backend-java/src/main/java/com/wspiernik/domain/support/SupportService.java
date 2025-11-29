package com.wspiernik.domain.support;

import com.wspiernik.api.websocket.ConversationSessionManager.ConversationSession;
import com.wspiernik.domain.events.ConversationCompletedEvent;
import com.wspiernik.infrastructure.llm.LlmClient;
import com.wspiernik.infrastructure.llm.PromptTemplates;
import com.wspiernik.infrastructure.llm.dto.LlmMessage;
import com.wspiernik.infrastructure.persistence.entity.CaregiverProfile;
import com.wspiernik.infrastructure.persistence.entity.CaregiverSupportLog;
import com.wspiernik.infrastructure.persistence.entity.Conversation;
import com.wspiernik.infrastructure.persistence.entity.Fact;
import com.wspiernik.infrastructure.persistence.repository.CaregiverProfileRepository;
import com.wspiernik.infrastructure.persistence.repository.CaregiverSupportLogRepository;
import com.wspiernik.infrastructure.persistence.repository.ConversationRepository;
import com.wspiernik.infrastructure.persistence.repository.FactRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service handling support conversation business logic.
 * Task 20: Support Service - LLM-powered emotional support
 */
@ApplicationScoped
public class SupportService {

    private static final Logger LOG = Logger.getLogger(SupportService.class);

    private static final String SUPPORT_STATE_KEY = "supportState";
    private static final String SUPPORT_COMPLETE_MARKER = "SUPPORT_COMPLETE";

    @Inject
    LlmClient llmClient;

    @Inject
    PromptTemplates promptTemplates;

    @Inject
    ConversationRepository conversationRepository;

    @Inject
    CaregiverProfileRepository profileRepository;

    @Inject
    FactRepository factRepository;

    @Inject
    CaregiverSupportLogRepository supportLogRepository;

    @Inject
    Event<ConversationCompletedEvent> conversationCompletedEvent;

    /**
     * Start a new support session.
     */
    public SupportStartResult startSupport(ConversationSession session, String initialMessage) {
        LOG.info("Starting support session");

        // Create support state
        SupportState state = new SupportState();

        // Create conversation record
        Long conversationId = QuarkusTransaction.requiringNew().call(() -> {
            Conversation conversation = new Conversation();
            conversation.conversationType = "support";
            conversation.startedAt = LocalDateTime.now();
            conversation.createdAt = LocalDateTime.now();
            conversationRepository.persist(conversation);
            return conversation.id;
        });

        state.setConversationId(conversationId);
        session.conversationId = conversationId;
        session.setContextValue(SUPPORT_STATE_KEY, state);

        // Generate welcome/initial response
        String response;
        if (initialMessage != null && !initialMessage.isBlank()) {
            // User sent initial message with start
            session.addMessage("user", initialMessage);
            state.incrementMessageCount();
            response = generateResponse(state, session);
        } else {
            // Generate greeting
            response = generateGreeting(state, session);
        }

        session.addMessage("assistant", response);
        state.incrementMessageCount();

        return new SupportStartResult(conversationId, response);
    }

    /**
     * Process user's message during support conversation.
     */
    public SupportMessageResult processMessage(ConversationSession session, String userMessage) {
        SupportState state = session.getContextValue(SUPPORT_STATE_KEY);

        if (state == null) {
            LOG.warn("No support state found in session");
            return SupportMessageResult.error("Nie znaleziono aktywnej sesji wsparcia");
        }

        // Add user message to history
        session.addMessage("user", userMessage);
        state.incrementMessageCount();

        // Generate response
        String response = generateResponse(state, session);

        // Check if LLM signaled completion
        if (response.contains(SUPPORT_COMPLETE_MARKER)) {
            state.setCompleted(true);
            response = response.replace(SUPPORT_COMPLETE_MARKER, "").trim();

            // Save support log
            saveSupportLog(state, session);
        }

        session.addMessage("assistant", response);
        state.incrementMessageCount();

        // Check if we should suggest ending
        boolean suggestEnd = state.shouldOfferSummary() && !state.isCompleted();

        return new SupportMessageResult(response, state.isCompleted(), suggestEnd);
    }

    /**
     * Complete the support session.
     */
    public SupportCompleteResult completeSupport(ConversationSession session) {
        SupportState state = session.getContextValue(SUPPORT_STATE_KEY);

        if (state == null) {
            return new SupportCompleteResult(null, "Nie znaleziono aktywnej sesji");
        }

        state.setCompleted(true);

        // Generate farewell message
        String farewell = generateFarewell(state, session);
        session.addMessage("assistant", farewell);

        // Save support log
        saveSupportLog(state, session);

        // Update conversation end time
        String transcript = buildTranscript(session);
        QuarkusTransaction.requiringNew().run(() -> {
            if (state.getConversationId() != null) {
                Conversation conversation = conversationRepository.findById(state.getConversationId());
                if (conversation != null) {
                    conversation.endedAt = LocalDateTime.now();
                    conversation.rawTranscript = transcript;
                    conversationRepository.persist(conversation);
                }
            }
        });

        // Fire event for facts extraction
        conversationCompletedEvent.fireAsync(new ConversationCompletedEvent(
                state.getConversationId(),
                session.caregiverId,
                "support",
                transcript,
                session.connectionId
        ));

        return new SupportCompleteResult(state.getConversationId(), farewell);
    }

    /**
     * Generate greeting message.
     */
    private String generateGreeting(SupportState state, ConversationSession session) {
        CaregiverProfile profile = getProfile();
        List<Fact> facts = getRecentFacts();

        String systemPrompt = promptTemplates.buildSupportPrompt(profile, facts);

        List<LlmMessage> messages = new ArrayList<>();
        messages.add(new LlmMessage("system", systemPrompt));
        messages.add(new LlmMessage("user",
                "[INSTRUKCJA: Przywitaj się ciepło z opiekunem i zapytaj jak się czuje. " +
                        "Bądź empatyczny i otwarty na rozmowę.]"));

        try {
            return llmClient.generateWithHistory(messages);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to generate greeting");
            return "Cześć! Jestem tu, żeby Cię wesprzeć. Jak się dzisiaj czujesz?";
        }
    }

    /**
     * Generate response to user message.
     */
    private String generateResponse(SupportState state, ConversationSession session) {
        CaregiverProfile profile = getProfile();
        List<Fact> facts = getRecentFacts();

        String systemPrompt = promptTemplates.buildSupportPrompt(profile, facts);

        List<LlmMessage> messages = new ArrayList<>();
        messages.add(new LlmMessage("system", systemPrompt));

        // Add conversation history
        messages.addAll(session.messageHistory);

        // Add context about conversation length
        if (state.shouldOfferSummary()) {
            messages.add(new LlmMessage("user",
                    "[INSTRUKCJA SYSTEMU: Rozmowa trwa już dłużej. " +
                            "Jeśli wydaje się, że opiekun chce zakończyć lub rozmowa dobiegła naturalnego końca, " +
                            "zaproponuj podsumowanie i zakończenie. Powiedz SUPPORT_COMPLETE na końcu.]"));
        }

        try {
            return llmClient.generateWithHistory(messages);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to generate response");
            return "Rozumiem. Proszę, powiedz mi więcej o tym, co czujesz.";
        }
    }

    /**
     * Generate farewell message.
     */
    private String generateFarewell(SupportState state, ConversationSession session) {
        String systemPrompt = """
                Jesteś wspierającym asystentem dla opiekuna. Rozmowa dobiega końca.
                Podsumuj krótko rozmowę, doceń wysiłek opiekuna i życz mu siły.
                Bądź ciepły i zachęcający. Maksymalnie 2-3 zdania.
                """;

        List<LlmMessage> messages = new ArrayList<>();
        messages.add(new LlmMessage("system", systemPrompt));
        messages.add(new LlmMessage("user", "[INSTRUKCJA: Pożegnaj się ciepło z opiekunem.]"));

        try {
            return llmClient.generateWithHistory(messages);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to generate farewell");
            return "Dziękuję za rozmowę. Pamiętaj, że robisz wspaniałą pracę jako opiekun. " +
                    "Jestem tu dla Ciebie, kiedy będziesz potrzebować wsparcia.";
        }
    }

    /**
     * Save support log entry.
     */
    private void saveSupportLog(SupportState state, ConversationSession session) {
        QuarkusTransaction.requiringNew().run(() -> {
            CaregiverSupportLog log = new CaregiverSupportLog();
            log.stressLevel = state.getStressLevel();
            log.needs = state.getIdentifiedNeeds();
            log.createdAt = LocalDateTime.now();
            supportLogRepository.persist(log);
            LOG.info("Saved support log entry");
        });
    }

    /**
     * Build transcript from message history.
     */
    private String buildTranscript(ConversationSession session) {
        StringBuilder sb = new StringBuilder();
        for (LlmMessage msg : session.messageHistory) {
            String role = "user".equals(msg.role()) ? "Opiekun" : "Stefan";
            sb.append(role).append(": ").append(msg.content()).append("\n\n");
        }
        return sb.toString();
    }

    /**
     * Get caregiver profile.
     */
    private CaregiverProfile getProfile() {
        return QuarkusTransaction.requiringNew().call(() ->
                profileRepository.findAll().firstResult()
        );
    }

    /**
     * Get recent facts.
     */
    private List<Fact> getRecentFacts() {
        return QuarkusTransaction.requiringNew().call(() ->
                factRepository.findRecentFacts(10)
        );
    }

    /**
     * Result of starting a support session.
     */
    public record SupportStartResult(
            Long conversationId,
            String greeting
    ) {}

    /**
     * Result of processing a support message.
     */
    public record SupportMessageResult(
            String response,
            boolean completed,
            boolean suggestEnd
    ) {
        public static SupportMessageResult error(String message) {
            return new SupportMessageResult(message, false, false);
        }
    }

    /**
     * Result of completing a support session.
     */
    public record SupportCompleteResult(
            Long conversationId,
            String farewell
    ) {}
}
