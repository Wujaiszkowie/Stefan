package com.wspiernik.domain.survey;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the state of an ongoing survey session.
 * Collects responses for each survey step.
 */
public class SurveyState {

    private SurveyStep currentStep;
    private final Map<SurveyStep, String> responses;
    private Long conversationId;
    private boolean awaitingConfirmation;

    public SurveyState() {
        this.currentStep = SurveyStep.WARD_AGE;
        this.responses = new HashMap<>();
        this.awaitingConfirmation = false;
    }

    public SurveyStep getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(SurveyStep step) {
        this.currentStep = step;
    }

    public Map<SurveyStep, String> getResponses() {
        return responses;
    }

    public void addResponse(SurveyStep step, String response) {
        responses.put(step, response);
    }

    public String getResponse(SurveyStep step) {
        return responses.get(step);
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public boolean isAwaitingConfirmation() {
        return awaitingConfirmation;
    }

    public void setAwaitingConfirmation(boolean awaitingConfirmation) {
        this.awaitingConfirmation = awaitingConfirmation;
    }

    /**
     * Move to the next step in the survey.
     * @return true if moved to next step, false if already completed
     */
    public boolean moveToNextStep() {
        SurveyStep next = currentStep.next();
        if (next != null) {
            currentStep = next;
            return true;
        }
        return false;
    }

    /**
     * Check if survey is completed.
     */
    public boolean isCompleted() {
        return currentStep.isCompleted();
    }

    /**
     * Get ward age from responses.
     */
    public String getWardAge() {
        return responses.get(SurveyStep.WARD_AGE);
    }

    /**
     * Get ward conditions from responses.
     */
    public String getWardConditions() {
        return responses.get(SurveyStep.WARD_CONDITIONS);
    }

    /**
     * Get ward medications from responses.
     */
    public String getWardMedications() {
        return responses.get(SurveyStep.WARD_MEDICATIONS);
    }

    /**
     * Get ward mobility info from responses.
     */
    public String getWardMobility() {
        return responses.get(SurveyStep.WARD_MOBILITY);
    }

    /**
     * Get other info from responses.
     */
    public String getWardOther() {
        return responses.get(SurveyStep.WARD_OTHER);
    }

    /**
     * Build a summary of all collected information.
     */
    public String buildSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Zebrane informacje o podopiecznym:\n\n");

        if (getWardAge() != null) {
            sb.append("Wiek: ").append(getWardAge()).append("\n");
        }
        if (getWardConditions() != null) {
            sb.append("Schorzenia: ").append(getWardConditions()).append("\n");
        }
        if (getWardMedications() != null) {
            sb.append("Leki: ").append(getWardMedications()).append("\n");
        }
        if (getWardMobility() != null) {
            sb.append("Mobilność: ").append(getWardMobility()).append("\n");
        }
        if (getWardOther() != null) {
            sb.append("Inne: ").append(getWardOther()).append("\n");
        }

        return sb.toString();
    }
}
