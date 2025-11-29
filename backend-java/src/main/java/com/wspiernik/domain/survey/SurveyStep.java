package com.wspiernik.domain.survey;

/**
 * Survey steps for gathering caregiver profile information.
 * Each step collects specific information about the ward.
 */
public enum SurveyStep {
    /**
     * Initial step - gather ward's age and basic info.
     */
    WARD_AGE(0, "Wiek podopiecznego"),

    /**
     * Gather information about ward's medical conditions.
     */
    WARD_CONDITIONS(1, "Schorzenia podopiecznego"),

    /**
     * Gather information about current medications.
     */
    WARD_MEDICATIONS(2, "Leki podopiecznego"),

    /**
     * Gather information about mobility and physical limitations.
     */
    WARD_MOBILITY(3, "Mobilność podopiecznego"),

    /**
     * Any other relevant information.
     */
    WARD_OTHER(4, "Inne informacje"),

    /**
     * Confirm collected information with user.
     */
    CONFIRMATION(5, "Potwierdzenie danych"),

    /**
     * Survey completed.
     */
    COMPLETED(6, "Zakończono");

    private final int order;
    private final String displayName;

    SurveyStep(int order, String displayName) {
        this.order = order;
        this.displayName = displayName;
    }

    public int getOrder() {
        return order;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the next step in the survey flow.
     * Returns null if this is the last step.
     */
    public SurveyStep next() {
        return switch (this) {
            case WARD_AGE -> WARD_CONDITIONS;
            case WARD_CONDITIONS -> WARD_MEDICATIONS;
            case WARD_MEDICATIONS -> WARD_MOBILITY;
            case WARD_MOBILITY -> WARD_OTHER;
            case WARD_OTHER -> CONFIRMATION;
            case CONFIRMATION -> COMPLETED;
            case COMPLETED -> null;
        };
    }

    /**
     * Check if this is the final step.
     */
    public boolean isCompleted() {
        return this == COMPLETED;
    }

    /**
     * Check if this is the confirmation step.
     */
    public boolean isConfirmation() {
        return this == CONFIRMATION;
    }

    /**
     * Get survey step by order number.
     */
    public static SurveyStep fromOrder(int order) {
        for (SurveyStep step : values()) {
            if (step.order == order) {
                return step;
            }
        }
        throw new IllegalArgumentException("Invalid survey step order: " + order);
    }
}
