package com.wspiernik.infrastructure.llm;

import com.wspiernik.infrastructure.llm.dto.LlmMessage;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Mock LLM client for development/testing when no real LLM is available.
 * Activated with: quarkus.profile=mock-llm or -Dquarkus.profile=mock-llm
 *
 * To use: add to application.properties:
 *   %dev.quarkus.arc.selected-alternatives=com.wspiernik.infrastructure.llm.MockLlmClient
 */
@Alternative
@ApplicationScoped
@IfBuildProfile("mock-llm")
public class MockLlmClient implements LlmClient {

    private static final Logger LOG = Logger.getLogger(MockLlmClient.class);

    @Override
    public String generate(String systemPrompt, String userMessage) {
        LOG.infof("MockLLM - System: %s...", systemPrompt.substring(0, Math.min(50, systemPrompt.length())));
        LOG.infof("MockLLM - User: %s", userMessage);

        return generateMockResponse(userMessage);
    }

    @Override
    public String generateWithContext(String systemPrompt, String userMessage, List<LlmMessage> history) {
        LOG.infof("MockLLM - With %d history messages", history != null ? history.size() : 0);
        return generate(systemPrompt, userMessage);
    }

    @Override
    public String generateWithHistory(List<LlmMessage> messages) {
        String lastUserMessage = messages.stream()
                .filter(m -> "user".equals(m.role()))
                .reduce((a, b) -> b)
                .map(LlmMessage::content)
                .orElse("(no user message)");

        return generateMockResponse(lastUserMessage);
    }

    private String generateMockResponse(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();

        // Survey responses
        if (lowerMessage.contains("lat") || lowerMessage.contains("wiek")) {
            return "Dziękuję za informację o wieku. Jakie choroby przewlekłe ma Twój podopieczny?";
        }
        if (lowerMessage.contains("cukrzyc") || lowerMessage.contains("nadciśn") || lowerMessage.contains("chorob")) {
            return "Rozumiem. Jakie leki obecnie przyjmuje podopieczny?";
        }
        if (lowerMessage.contains("lek") || lowerMessage.contains("tabletk")) {
            return "Dziękuję. Czy podopieczny ma jakieś ograniczenia ruchowe?";
        }

        // Intervention responses
        if (lowerMessage.contains("upad") || lowerMessage.contains("przewróc")) {
            return "Rozumiem, że doszło do upadku. Czy podopieczny stracił przytomność?";
        }
        if (lowerMessage.contains("ból") || lowerMessage.contains("boli")) {
            return "Gdzie dokładnie odczuwa ból? Proszę opisać lokalizację.";
        }

        // Support responses
        if (lowerMessage.contains("zmęcz") || lowerMessage.contains("stres") || lowerMessage.contains("trudn")) {
            return "Rozumiem, że to dla Ciebie trudne. Opieka nad bliską osobą jest wymagająca. Chcesz mi opowiedzieć więcej?";
        }

        // Default response
        return "Dziękuję za wiadomość. Czy mogę jakoś pomóc? [MOCK RESPONSE]";
    }
}
