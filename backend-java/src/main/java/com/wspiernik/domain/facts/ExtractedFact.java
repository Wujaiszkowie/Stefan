package com.wspiernik.domain.facts;

/**
 * DTO representing a fact extracted from a conversation by the LLM.
 */
public record ExtractedFact(
        String type,       // "symptom", "medication", "event", "condition", "limitation"
        String value,      // Short description of the fact
        Integer severity,  // 1-10 (nullable, mainly for symptoms and events)
        String context     // Additional context if needed
) {
    /**
     * Check if this is a valid fact with required fields.
     */
    public boolean isValid() {
        return type != null && !type.isBlank() && value != null && !value.isBlank();
    }
}
