package com.wspiernik.infrastructure.llm;

import com.wspiernik.infrastructure.llm.dto.LlmMessage;
import com.wspiernik.infrastructure.llm.dto.LlmRequest;
import com.wspiernik.infrastructure.llm.dto.LlmResponse;
import com.wspiernik.infrastructure.persistence.repository.BackendLogRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of LlmClient using Bielnik LLM via REST API.
 * Provides error handling, logging, and configuration management.
 */
@ApplicationScoped
public class BielnikClient implements LlmClient {

    private static final Logger LOG = Logger.getLogger(BielnikClient.class);
    private static final String MODULE = "LLM";
    private static final String ERROR_MESSAGE = "Przepraszam, wystąpił problem z generowaniem odpowiedzi. Proszę spróbować ponownie.";

    @Inject
    @RestClient
    BielnikApi bielnikApi;

    @Inject
    BackendLogRepository logRepository;

    @ConfigProperty(name = "wspiernik.llm.model", defaultValue = "bielik")
    String model;

    @ConfigProperty(name = "wspiernik.llm.temperature", defaultValue = "0.7")
    double temperature;

    @ConfigProperty(name = "wspiernik.llm.max-tokens", defaultValue = "2048")
    int maxTokens;

    @Override
    public String generate(String systemPrompt, String userMessage) {
        List<LlmMessage> messages = new ArrayList<>();
        messages.add(LlmMessage.system(systemPrompt));
        messages.add(LlmMessage.user(userMessage));

        return generateWithHistory(messages);
    }

    @Override
    public String generateWithContext(String systemPrompt, String userMessage, List<LlmMessage> history) {
        List<LlmMessage> messages = new ArrayList<>();
        messages.add(LlmMessage.system(systemPrompt));

        if (history != null && !history.isEmpty()) {
            messages.addAll(history);
        }

        messages.add(LlmMessage.user(userMessage));

        return generateWithHistory(messages);
    }

    @Override
    public String generateWithHistory(List<LlmMessage> messages) {
        LOG.debugf("Sending request to LLM with %d messages", messages.size());

        try {
            LlmRequest request = LlmRequest.builder()
                    .model(model)
                    .messages(messages)
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .build();

            LlmResponse response = bielnikApi.chatCompletion(request);

            if (response == null) {
                LOG.error("LLM returned null response");
                logRepository.logError(MODULE, "LLM returned null response", null);
                return ERROR_MESSAGE;
            }

            String content = response.getContent();

            if (content == null || content.isBlank()) {
                LOG.error("LLM returned empty content");
                logRepository.logError(MODULE, "LLM returned empty content", null);
                return ERROR_MESSAGE;
            }

            LOG.debugf("LLM response received: %d characters", content.length());
            return content;

        } catch (Exception e) {
            LOG.errorf(e, "Error calling LLM API: %s", e.getMessage());
            logRepository.logError(MODULE, "Error calling LLM API",
                    String.format("{\"error\": \"%s\", \"type\": \"%s\"}",
                            e.getMessage(), e.getClass().getSimpleName()));
            return ERROR_MESSAGE;
        }
    }
}
