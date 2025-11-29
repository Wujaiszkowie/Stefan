package com.wspiernik.infrastructure.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request payload for LLM chat completions API.
 * Compatible with OpenAI API format.
 */
public record LlmRequest(
        @JsonProperty("model") String model,
        @JsonProperty("messages") List<LlmMessage> messages,
        @JsonProperty("temperature") double temperature,
        @JsonProperty("max_tokens") int maxTokens
) {
    /**
     * Builder for creating LlmRequest instances.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String model = "bielik";
        private List<LlmMessage> messages;
        private double temperature = 0.7;
        private int maxTokens = 2048;

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder messages(List<LlmMessage> messages) {
            this.messages = messages;
            return this;
        }

        public Builder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public LlmRequest build() {
            return new LlmRequest(model, messages, temperature, maxTokens);
        }
    }
}
