package com.wspiernik.infrastructure.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a choice in the LLM response.
 * Compatible with OpenAI API format.
 */
public record LlmChoice(
        @JsonProperty("index") int index,
        @JsonProperty("message") LlmMessage message,
        @JsonProperty("finish_reason") String finishReason
) {
}
