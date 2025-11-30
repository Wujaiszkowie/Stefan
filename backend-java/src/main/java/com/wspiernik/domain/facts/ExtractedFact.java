package com.wspiernik.domain.facts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO representing a fact extracted from a conversation by the LLM.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExtractedFact(
        @JsonProperty("tags")
        List<String> tags,
        @JsonProperty("value")
        String value,
        @JsonProperty("severity")
        Integer severity,
        @JsonProperty("context")
        Integer context
) {
    /**
     * Check if this is a valid fact with required fields.
     */
    public boolean isValid() {
        return tags != null && !tags.isEmpty() && value != null && !value.isBlank();
    }
}
