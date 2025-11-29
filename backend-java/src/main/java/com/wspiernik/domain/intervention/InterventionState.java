package com.wspiernik.domain.intervention;

import com.wspiernik.infrastructure.persistence.entity.CrisisScenario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds the state of an ongoing intervention session.
 * Task 17: Intervention State Machine - State holder
 */
public class InterventionState {

    private CrisisScenario scenario;
    private String situationDescription;
    private int currentQuestionIndex;
    private List<String> questions;
    private final Map<Integer, String> responses;
    private Long conversationId;
    private boolean completed;
    private boolean isGenericIntervention;

    public InterventionState() {
        this.currentQuestionIndex = 0;
        this.questions = new ArrayList<>();
        this.responses = new HashMap<>();
        this.completed = false;
        this.isGenericIntervention = false;
    }

    /**
     * Initialize state with a matched scenario.
     */
    public void initializeWithScenario(CrisisScenario scenario, List<String> parsedQuestions) {
        this.scenario = scenario;
        this.questions = new ArrayList<>(parsedQuestions);
        this.isGenericIntervention = false;
    }

    /**
     * Initialize state for generic intervention (no scenario matched).
     */
    public void initializeAsGeneric(String situationDescription) {
        this.scenario = null;
        this.situationDescription = situationDescription;
        this.isGenericIntervention = true;
        // For generic intervention, LLM will guide the conversation
    }

    public CrisisScenario getScenario() {
        return scenario;
    }

    public String getSituationDescription() {
        return situationDescription;
    }

    public void setSituationDescription(String situationDescription) {
        this.situationDescription = situationDescription;
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public List<String> getQuestions() {
        return questions;
    }

    /**
     * Get current question (for scenario-based intervention).
     */
    public String getCurrentQuestion() {
        if (questions.isEmpty() || currentQuestionIndex >= questions.size()) {
            return null;
        }
        return questions.get(currentQuestionIndex);
    }

    /**
     * Check if there are more questions.
     */
    public boolean hasMoreQuestions() {
        return !questions.isEmpty() && currentQuestionIndex < questions.size();
    }

    /**
     * Move to next question.
     */
    public void moveToNextQuestion() {
        if (currentQuestionIndex < questions.size()) {
            currentQuestionIndex++;
        }
    }

    /**
     * Store response for current question.
     */
    public void addResponse(String response) {
        responses.put(currentQuestionIndex, response);
    }

    public Map<Integer, String> getResponses() {
        return responses;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isGenericIntervention() {
        return isGenericIntervention;
    }

    /**
     * Build a summary of the intervention.
     */
    public String buildSummary() {
        StringBuilder sb = new StringBuilder();

        if (scenario != null) {
            sb.append("Typ interwencji: ").append(scenario.name).append("\n\n");
        } else {
            sb.append("Typ interwencji: Ogólna\n");
            sb.append("Opis sytuacji: ").append(situationDescription).append("\n\n");
        }

        sb.append("Zebrane informacje:\n");
        for (int i = 0; i < questions.size() && i < responses.size(); i++) {
            String question = questions.get(i);
            String response = responses.get(i);
            if (response != null) {
                sb.append("P: ").append(question).append("\n");
                sb.append("O: ").append(response).append("\n\n");
            }
        }

        return sb.toString();
    }

    /**
     * Get scenario key or "generic" if no scenario.
     */
    public String getScenarioKey() {
        return scenario != null ? scenario.scenarioKey : "generic";
    }

    /**
     * Get scenario name or "Ogólna interwencja" if no scenario.
     */
    public String getScenarioName() {
        return scenario != null ? scenario.name : "Ogólna interwencja";
    }
}
