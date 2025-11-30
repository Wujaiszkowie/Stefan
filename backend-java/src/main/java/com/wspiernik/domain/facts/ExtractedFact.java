package com.wspiernik.domain.facts;

import java.util.List;

/**
 * DTO representing a fact extracted from a conversation by the LLM.
 */
public record ExtractedFact(
        List<String> tags,
        String value,
        Integer severity
) {
    /**
     * Check if this is a valid fact with required fields.
     */
    public boolean isValid() {
        return tags != null && !tags.isEmpty() && value != null && !value.isBlank();
    }
}
