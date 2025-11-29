package com.wspiernik.domain.facts;

/**
 * DTO representing a fact extracted from a conversation by the LLM.
 */
public record ExtractedFact(
        String tags,
        String value,
        Integer severity
) {
    /**
     * Check if this is a valid fact with required fields.
     */
    public boolean isValid() {
        return tags != null && !tags.isBlank() && value != null && !value.isBlank();
    }
}
