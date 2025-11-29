package com.wspiernik.infrastructure.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response from LLM chat completions API.
 * Compatible with OpenAI API format.
 */
public record LlmResponse(
        @JsonProperty("id") String id,
        @JsonProperty("object") String object,
        @JsonProperty("created") long created,
        @JsonProperty("model") String model,
        @JsonProperty("choices") List<LlmChoice> choices,
        @JsonProperty("usage") Usage usage
) {
    /**
     * Get the content of the first choice's message.
     * This is the typical way to get the assistant's response.
     */
    public String getContent() {
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        LlmMessage message = choices.get(0).message();
        return message != null ? message.content() : null;
    }

    /**
     * Usage statistics for the request.
     */
    public record Usage(
            @JsonProperty("prompt_tokens") int promptTokens,
            @JsonProperty("completion_tokens") int completionTokens,
            @JsonProperty("total_tokens") int totalTokens
    ) {
    }
}
