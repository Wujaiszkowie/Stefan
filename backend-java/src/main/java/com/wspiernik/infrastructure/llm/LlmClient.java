package com.wspiernik.infrastructure.llm;

import com.wspiernik.infrastructure.llm.dto.LlmMessage;

import java.util.List;

/**
 * Interface for LLM (Large Language Model) client operations.
 * Abstracts the communication with the underlying LLM service.
 */
public interface LlmClient {

    /**
     * Generate a response using system prompt and user message.
     *
     * @param systemPrompt The system prompt that sets the context/behavior
     * @param userMessage  The user's message
     * @return The assistant's response content
     */
    String generate(String systemPrompt, String userMessage);

    /**
     * Generate a response with conversation history context.
     *
     * @param systemPrompt The system prompt
     * @param userMessage  The new user message
     * @param history      Previous conversation history
     * @return The assistant's response content
     */
    String generateWithContext(String systemPrompt, String userMessage, List<LlmMessage> history);

    /**
     * Generate a response from a complete message list.
     *
     * @param messages The full list of messages including system, user, and assistant messages
     * @return The assistant's response content
     */
    String generateWithHistory(List<LlmMessage> messages);
}
