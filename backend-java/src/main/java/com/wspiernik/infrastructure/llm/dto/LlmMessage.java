package com.wspiernik.infrastructure.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a message in the LLM conversation.
 * Compatible with OpenAI chat completions API format.
 */
public record LlmMessage(
        @JsonProperty("role") String role,      // "system", "user", "assistant"
        @JsonProperty("content") String content
) {
    /**
     * Create a system message.
     */
    public static LlmMessage system(String content) {
        return new LlmMessage("system", content);
    }

    /**
     * Create a user message.
     */
    public static LlmMessage user(String content) {
        return new LlmMessage("user", content);
    }

    /**
     * Create an assistant message.
     */
    public static LlmMessage assistant(String content) {
        return new LlmMessage("assistant", content);
    }
}
