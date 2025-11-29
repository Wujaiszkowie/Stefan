package com.wspiernik.domain.intervention;

import com.wspiernik.api.websocket.ConversationSessionManager.ConversationSession;
import com.wspiernik.domain.intervention.ScenarioMatchingService.MatchResult;
import com.wspiernik.infrastructure.llm.LlmClient;
import com.wspiernik.infrastructure.llm.PromptTemplates;
import com.wspiernik.infrastructure.llm.dto.LlmMessage;
import com.wspiernik.infrastructure.persistence.entity.CaregiverProfile;
import com.wspiernik.infrastructure.persistence.entity.Conversation;
import com.wspiernik.infrastructure.persistence.entity.CrisisScenario;
import com.wspiernik.infrastructure.persistence.entity.Fact;
import com.wspiernik.infrastructure.persistence.repository.CaregiverProfileRepository;
import com.wspiernik.infrastructure.persistence.repository.ConversationRepository;
import com.wspiernik.infrastructure.persistence.repository.FactRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service handling intervention business logic.
 * Task 17: Intervention State Machine
 */
@ApplicationScoped
public class InterventionService {

    private static final Logger LOG = Logger.getLogger(InterventionService.class);

    private static final String INTERVENTION_STATE_KEY = "interventionState";
    private static final String INTERVENTION_COMPLETE_MARKER = "INTERVENTION_COMPLETE";

    @Inject
    ScenarioMatchingService scenarioMatchingService;

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

    /**
     * Start a new intervention session.
     * Attempts to match the situation description to a crisis scenario.
     */
    public InterventionStartResult startIntervention(ConversationSession session, String situationDescription) {
        LOG.infof("Starting intervention for situation: %s", situationDescription);

        // Try to match scenario
        MatchResult matchResult = scenarioMatchingService.matchScenario(situationDescription);

        // Create intervention state
        InterventionState state = new InterventionState();
        state.setSituationDescription(situationDescription);

        // Create conversation record
        Long conversationId = QuarkusTransaction.requiringNew().call(() -> {
            Conversation conversation = new Conversation();
            conversation.conversationType = "intervention";
            conversation.scenarioType = matchResult.matched() ? matchResult.scenario().scenarioKey : null;
            conversation.startedAt = LocalDateTime.now();
            conversation.createdAt = LocalDateTime.now();
            conversationRepository.persist(conversation);
            return conversation.id;
        });

        state.setConversationId(conversationId);
        session.conversationId = conversationId;

        String firstMessage;

        if (matchResult.matched()) {
            // Initialize with matched scenario
            CrisisScenario scenario = matchResult.scenario();
            List<String> questions = scenarioMatchingService.parseQuestions(scenario.questionsSequence);
            state.initializeWithScenario(scenario, questions);

            LOG.infof("Matched scenario: %s", scenario.scenarioKey);

            // Generate first question using LLM with scenario context
            firstMessage = generateScenarioResponse(state, session, situationDescription);

        } else {
            // Generic intervention
            state.initializeAsGeneric(situationDescription);

            LOG.info("No scenario matched, using generic intervention");

            // Generate generic intervention response
            firstMessage = generateGenericResponse(state, session, situationDescription);
        }

        // Store state in session
        session.setContextValue(INTERVENTION_STATE_KEY, state);
        session.addMessage("assistant", firstMessage);

        return new InterventionStartResult(
                conversationId,
                matchResult.matched(),
                state.getScenarioKey(),
                state.getScenarioName(),
                firstMessage,
                state.getCurrentQuestionIndex()
        );
    }

    /**
     * Process user's response during intervention.
     */
    public InterventionMessageResult processMessage(ConversationSession session, String userMessage) {
        InterventionState state = session.getContextValue(INTERVENTION_STATE_KEY);

        if (state == null) {
            LOG.warn("No intervention state found in session");
            return InterventionMessageResult.error("Nie znaleziono aktywnej interwencji");
        }

        // Add user message to history
        session.addMessage("user", userMessage);

        // Store response for current question
        state.addResponse(userMessage);

        String response;

        if (state.isGenericIntervention()) {
            // For generic intervention, always use LLM
            response = generateGenericResponse(state, session, userMessage);
        } else {
            // For scenario-based intervention
            state.moveToNextQuestion();

            if (state.hasMoreQuestions()) {
                // Generate next question with LLM
                response = generateScenarioResponse(state, session, userMessage);
            } else {
                // All questions answered, generate summary
                response = generateSummary(state, session);
                state.setCompleted(true);
            }
        }

        // Check if LLM signaled completion
        if (response.contains(INTERVENTION_COMPLETE_MARKER)) {
            state.setCompleted(true);
            response = response.replace(INTERVENTION_COMPLETE_MARKER, "").trim();
        }

        session.addMessage("assistant", response);

        return new InterventionMessageResult(
                response,
                state.getCurrentQuestionIndex(),
                state.isCompleted()
        );
    }

    /**
     * Generate response using scenario's system prompt.
     */
    private String generateScenarioResponse(InterventionState state, ConversationSession session, String contextMessage) {
        CrisisScenario scenario = state.getScenario();

        // Get profile and facts
        CaregiverProfile profile = getProfile();
        List<Fact> facts = getRecentFacts();

        // Build system prompt from scenario
        String systemPrompt = promptTemplates.buildInterventionPrompt(profile, facts, scenario);

        List<LlmMessage> messages = new ArrayList<>();
        messages.add(new LlmMessage("system", systemPrompt));

        // Add conversation history
        messages.addAll(session.messageHistory);

        // Add current question hint if available
        String currentQuestion = state.getCurrentQuestion();
        if (currentQuestion != null) {
            messages.add(new LlmMessage("user",
                    "[INSTRUKCJA: Następne pytanie do zadania: " + currentQuestion + ". " +
                            "Zadaj je w naturalny sposób, biorąc pod uwagę kontekst rozmowy.]"));
        }

        try {
            return llmClient.generateWithHistory(messages);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to generate scenario response, using fallback");
            return currentQuestion != null ? currentQuestion : "Proszę opisz sytuację bardziej szczegółowo.";
        }
    }

    /**
     * Generate response for generic intervention.
     */
    private String generateGenericResponse(InterventionState state, ConversationSession session, String contextMessage) {
        // Get profile and facts
        CaregiverProfile profile = getProfile();
        List<Fact> facts = getRecentFacts();

        // Build generic intervention prompt
        String systemPrompt = promptTemplates.buildGenericInterventionPrompt(
                profile, facts, state.getSituationDescription());

        List<LlmMessage> messages = new ArrayList<>();
        messages.add(new LlmMessage("system", systemPrompt));
        messages.addAll(session.messageHistory);

        try {
            return llmClient.generateWithHistory(messages);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to generate generic response, using fallback");
            return "Proszę opisz dokładniej co się dzieje z podopiecznym.";
        }
    }

    /**
     * Generate final summary for the intervention.
     */
    private String generateSummary(InterventionState state, ConversationSession session) {
        String summary = state.buildSummary();

        // Get profile and facts for context
        CaregiverProfile profile = getProfile();
        List<Fact> facts = getRecentFacts();

        String systemPrompt = """
                Jesteś asystentem podsumowującym interwencję kryzysową.

                Na podstawie zebranych informacji:
                1. Podsumuj sytuację
                2. Oceń powagę (niskie/średnie/wysokie ryzyko)
                3. Podaj konkretne zalecenia
                4. Jeśli sytuacja wymaga pilnej pomocy medycznej, wyraźnie to zaznacz

                Bądź konkretny i rzeczowy. Zakończ słowem INTERVENTION_COMPLETE.
                """;

        List<LlmMessage> messages = new ArrayList<>();
        messages.add(new LlmMessage("system", systemPrompt));
        messages.add(new LlmMessage("user", "Podsumuj poniższą interwencję:\n\n" + summary));

        try {
            String response = llmClient.generateWithHistory(messages);

            // Update conversation end time
            QuarkusTransaction.requiringNew().run(() -> {
                if (state.getConversationId() != null) {
                    Conversation conversation = conversationRepository.findById(state.getConversationId());
                    if (conversation != null) {
                        conversation.endedAt = LocalDateTime.now();
                        conversation.rawTranscript = summary;
                        conversationRepository.persist(conversation);
                    }
                }
            });

            return response;
        } catch (Exception e) {
            LOG.errorf(e, "Failed to generate summary");
            return "Interwencja zakończona. " + summary + "\n\nINTERVENTION_COMPLETE";
        }
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
     * Result of starting an intervention.
     */
    public record InterventionStartResult(
            Long conversationId,
            boolean scenarioMatched,
            String scenarioKey,
            String scenarioName,
            String firstMessage,
            int currentStep
    ) {}

    /**
     * Result of processing an intervention message.
     */
    public record InterventionMessageResult(
            String response,
            int currentStep,
            boolean completed
    ) {
        public static InterventionMessageResult error(String message) {
            return new InterventionMessageResult(message, 0, false);
        }
    }
}
