package com.wspiernik.domain.survey;

import com.wspiernik.api.websocket.ConversationSessionManager.ConversationSession;
import com.wspiernik.domain.events.ConversationCompletedEvent;
import com.wspiernik.infrastructure.llm.LlmClient;
import com.wspiernik.infrastructure.llm.dto.LlmMessage;
import com.wspiernik.infrastructure.persistence.entity.CaregiverProfile;
import com.wspiernik.infrastructure.persistence.entity.Conversation;
import com.wspiernik.infrastructure.persistence.repository.CaregiverProfileRepository;
import com.wspiernik.infrastructure.persistence.repository.ConversationRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Service handling survey business logic.
 * Manages the state machine for gathering caregiver profile information.
 */
@ApplicationScoped
public class SurveyService {

    private static final Logger LOG = Logger.getLogger(SurveyService.class);

    private static final String SURVEY_STATE_KEY = "surveyState";

    @Inject
    LlmClient llmClient;

    @Inject
    ConversationRepository conversationRepository;

    @Inject
    CaregiverProfileRepository profileRepository;

    @Inject
    Event<ConversationCompletedEvent> conversationCompletedEvent;

    /**
     * Start a new survey session.
     * Creates conversation record and returns initial question.
     */
    public SurveyStartResult startSurvey(ConversationSession session) {
        LOG.info("Starting new survey");

        // Create survey state
        SurveyState state = new SurveyState();

        // Create conversation record in database
        Long conversationId = QuarkusTransaction.requiringNew().call(() -> {
            Conversation conversation = new Conversation();
            conversation.conversationType = "survey";
            conversation.startedAt = java.time.LocalDateTime.now();
            conversation.createdAt = java.time.LocalDateTime.now();
            conversationRepository.persist(conversation);
            return conversation.id;
        });

        state.setConversationId(conversationId);
        session.conversationId = conversationId;
        session.setContextValue(SURVEY_STATE_KEY, state);

        // Generate first question using LLM
        String firstQuestion = generateQuestion(state, session.messageHistory);

        // Add assistant message to history
        session.addMessage("assistant", firstQuestion);

        return new SurveyStartResult(conversationId, firstQuestion, state.getCurrentStep());
    }

    /**
     * Process user's response and generate next question.
     */
    public SurveyMessageResult processMessage(ConversationSession session, String userMessage) {
        SurveyState state = session.getContextValue(SURVEY_STATE_KEY);

        if (state == null) {
            LOG.warn("No survey state found in session");
            return SurveyMessageResult.error("Nie znaleziono aktywnej ankiety");
        }

        // Add user message to history
        session.addMessage("user", userMessage);

        // Handle confirmation step specially
        if (state.getCurrentStep().isConfirmation()) {
            return handleConfirmation(session, state, userMessage);
        }

        // Store response for current step
        state.addResponse(state.getCurrentStep(), userMessage);

        // Move to next step
        state.moveToNextStep();

        // Check if we need to show confirmation
        if (state.getCurrentStep().isConfirmation()) {
            String summary = state.buildSummary();
            String confirmationMessage = generateConfirmationMessage(summary, session.messageHistory);
            session.addMessage("assistant", confirmationMessage);
            state.setAwaitingConfirmation(true);
            return new SurveyMessageResult(confirmationMessage, state.getCurrentStep(), false);
        }

        // Generate next question
        String nextQuestion = generateQuestion(state, session.messageHistory);
        session.addMessage("assistant", nextQuestion);

        return new SurveyMessageResult(nextQuestion, state.getCurrentStep(), false);
    }

    /**
     * Handle confirmation step response.
     */
    private SurveyMessageResult handleConfirmation(ConversationSession session, SurveyState state, String userMessage) {
        String lowerMessage = userMessage.toLowerCase().trim();

        // Check for confirmation
        if (lowerMessage.contains("tak") || lowerMessage.contains("zgadza") ||
            lowerMessage.contains("potwierdz") || lowerMessage.contains("ok") ||
            lowerMessage.contains("dobrze")) {

            // Move to completed
            state.moveToNextStep();

            // Save profile
            saveCaregiverProfile(state);

            // Fire event for facts extraction
            String transcript = buildTranscript(session);
            conversationCompletedEvent.fireAsync(new ConversationCompletedEvent(
                    state.getConversationId(),
                    null,  // caregiverId not set for survey
                    "survey",
                    transcript,
                    session.connectionId
            ));

            String completionMessage = "Dziękuję! Twój profil został zapisany. " +
                    "Teraz będę mógł lepiej Ci pomagać w opiece nad podopiecznym.";
            session.addMessage("assistant", completionMessage);

            return new SurveyMessageResult(completionMessage, state.getCurrentStep(), true);
        }

        // Check for correction request
        if (lowerMessage.contains("nie") || lowerMessage.contains("poprawk") ||
            lowerMessage.contains("zmień") || lowerMessage.contains("błąd")) {

            // Reset to first step for re-entry
            state.setCurrentStep(SurveyStep.WARD_AGE);
            state.getResponses().clear();
            state.setAwaitingConfirmation(false);

            String retryMessage = "Rozumiem. Zacznijmy od początku. " +
                    generateQuestion(state, session.messageHistory);
            session.addMessage("assistant", retryMessage);

            return new SurveyMessageResult(retryMessage, state.getCurrentStep(), false);
        }

        // Unclear response, ask again
        String clarifyMessage = "Przepraszam, nie zrozumiałem. " +
                "Czy dane są poprawne? Odpowiedz 'tak' aby potwierdzić lub 'nie' aby wprowadzić poprawki.";
        session.addMessage("assistant", clarifyMessage);

        return new SurveyMessageResult(clarifyMessage, state.getCurrentStep(), false);
    }

    /**
     * Generate a question for the current survey step using LLM.
     */
    private String generateQuestion(SurveyState state, List<LlmMessage> history) {
        String systemPrompt = buildSurveySystemPrompt(state);

        List<LlmMessage> messages = new ArrayList<>();
        messages.add(new LlmMessage("system", systemPrompt));
        messages.addAll(history);

        // Add instruction for next question
        String stepInstruction = getStepInstruction(state.getCurrentStep());
        messages.add(new LlmMessage("user", "[INSTRUKCJA SYSTEMU: " + stepInstruction + "]"));

        try {
            return llmClient.generateWithHistory(messages);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to generate survey question, using fallback");
            return getFallbackQuestion(state.getCurrentStep());
        }
    }

    /**
     * Generate confirmation message using LLM.
     */
    private String generateConfirmationMessage(String summary, List<LlmMessage> history) {
        String systemPrompt = """
                Jesteś asystentem opiekuna osoby starszej. Właśnie zebrałeś informacje o podopiecznym.
                Przedstaw zebrane dane w przyjazny sposób i poproś o potwierdzenie.
                Bądź ciepły i empatyczny.
                """;

        List<LlmMessage> messages = new ArrayList<>();
        messages.add(new LlmMessage("system", systemPrompt));
        messages.add(new LlmMessage("user",
                "[INSTRUKCJA: Przedstaw poniższe dane i poproś o potwierdzenie]\n\n" + summary));

        try {
            return llmClient.generateWithHistory(messages);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to generate confirmation message, using fallback");
            return "Oto zebrane informacje:\n\n" + summary +
                    "\nCzy wszystko się zgadza? Odpowiedz 'tak' aby potwierdzić.";
        }
    }

    /**
     * Build system prompt for survey conversation.
     */
    private String buildSurveySystemPrompt(SurveyState state) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
                Jesteś Stefanem, ciepłym i empatycznym asystentem dla opiekunów osób starszych.
                Prowadzisz krótką rozmowę, aby zebrać podstawowe informacje o podopiecznym.

                Zasady:
                - Zadawaj jedno pytanie na raz
                - Bądź cierpliwy i wyrozumiały
                - Używaj prostego, przyjaznego języka
                - Nie powtarzaj informacji, które już zebrałeś
                - Odpowiadaj krótko (1-2 zdania)

                """);

        // Add context of what we already know
        if (!state.getResponses().isEmpty()) {
            prompt.append("Już wiesz:\n");
            for (var entry : state.getResponses().entrySet()) {
                prompt.append("- ").append(entry.getKey().getDisplayName())
                        .append(": ").append(entry.getValue()).append("\n");
            }
        }

        return prompt.toString();
    }

    /**
     * Get instruction for generating question for specific step.
     */
    private String getStepInstruction(SurveyStep step) {
        return switch (step) {
            case WARD_AGE -> "Zapytaj o wiek podopiecznego w naturalny sposób.";
            case WARD_CONDITIONS -> "Zapytaj o główne schorzenia lub problemy zdrowotne podopiecznego.";
            case WARD_MEDICATIONS -> "Zapytaj jakie leki przyjmuje podopieczny.";
            case WARD_MOBILITY -> "Zapytaj o mobilność podopiecznego - czy chodzi samodzielnie, używa laski/chodzika, jest na wózku.";
            case WARD_OTHER -> "Zapytaj czy jest coś jeszcze ważnego o podopiecznym, co powinneś wiedzieć.";
            case CONFIRMATION, COMPLETED -> "";
        };
    }

    /**
     * Get fallback question if LLM fails.
     */
    private String getFallbackQuestion(SurveyStep step) {
        return switch (step) {
            case WARD_AGE -> "Ile lat ma Twój podopieczny?";
            case WARD_CONDITIONS -> "Jakie ma główne schorzenia lub problemy zdrowotne?";
            case WARD_MEDICATIONS -> "Jakie leki obecnie przyjmuje?";
            case WARD_MOBILITY -> "Jak wygląda mobilność podopiecznego? Czy porusza się samodzielnie?";
            case WARD_OTHER -> "Czy jest jeszcze coś ważnego, co powinienem wiedzieć?";
            case CONFIRMATION -> "Czy dane są poprawne?";
            case COMPLETED -> "Dziękuję za wypełnienie ankiety!";
        };
    }

    /**
     * Save collected profile data to database.
     */
    private void saveCaregiverProfile(SurveyState state) {
        QuarkusTransaction.requiringNew().run(() -> {
            // Check if profile already exists
            CaregiverProfile profile = profileRepository.findAll().firstResult();

            if (profile == null) {
                profile = new CaregiverProfile();
            }

            // Update profile with survey data
            profile.wardAge = parseAge(state.getWardAge());
            profile.wardConditions = state.getWardConditions();
            profile.wardMedications = state.getWardMedications();
            profile.wardMobilityLimits = state.getWardMobility();
            profile.wardOtherInfo = state.getWardOther();
            profile.updatedAt = java.time.LocalDateTime.now();
            if (profile.createdAt == null) {
                profile.createdAt = java.time.LocalDateTime.now();
            }

            profileRepository.persist(profile);

            // Update conversation end time
            if (state.getConversationId() != null) {
                Conversation conversation = conversationRepository.findById(state.getConversationId());
                if (conversation != null) {
                    conversation.endedAt = java.time.LocalDateTime.now();
                    conversationRepository.persist(conversation);
                }
            }

            LOG.info("Saved caregiver profile from survey");
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
     * Parse age from user response.
     */
    private Integer parseAge(String ageResponse) {
        if (ageResponse == null) return null;

        // Try to extract number from response
        String digits = ageResponse.replaceAll("[^0-9]", "");
        if (!digits.isEmpty()) {
            try {
                return Integer.parseInt(digits);
            } catch (NumberFormatException e) {
                LOG.warnf("Could not parse age from: %s", ageResponse);
            }
        }
        return null;
    }

    /**
     * Result of starting a survey.
     */
    public record SurveyStartResult(
            Long conversationId,
            String question,
            SurveyStep currentStep
    ) {}

    /**
     * Result of processing a survey message.
     */
    public record SurveyMessageResult(
            String response,
            SurveyStep currentStep,
            boolean completed
    ) {
        public static SurveyMessageResult error(String message) {
            return new SurveyMessageResult(message, null, false);
        }
    }
}
