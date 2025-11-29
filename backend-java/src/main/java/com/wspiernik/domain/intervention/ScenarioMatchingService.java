package com.wspiernik.domain.intervention;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wspiernik.infrastructure.persistence.entity.CrisisScenario;
import com.wspiernik.infrastructure.persistence.repository.CrisisScenarioRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for matching user input against crisis scenario trigger keywords.
 * Task 16: Scenario Matching Service
 */
@ApplicationScoped
public class ScenarioMatchingService {

    private static final Logger LOG = Logger.getLogger(ScenarioMatchingService.class);

    @Inject
    CrisisScenarioRepository scenarioRepository;

    @Inject
    ObjectMapper objectMapper;

    /**
     * Result of scenario matching operation.
     */
    public record MatchResult(
            boolean matched,
            CrisisScenario scenario,
            String matchedKeyword,
            double confidence
    ) {
        public static MatchResult noMatch() {
            return new MatchResult(false, null, null, 0.0);
        }

        public static MatchResult matched(CrisisScenario scenario, String keyword, double confidence) {
            return new MatchResult(true, scenario, keyword, confidence);
        }
    }

    /**
     * Try to match user input against all available crisis scenarios.
     * Returns the best matching scenario based on keyword matches.
     *
     * @param userInput The user's description of the situation
     * @return MatchResult with the matched scenario or noMatch if none found
     */
    public MatchResult matchScenario(String userInput) {
        if (userInput == null || userInput.isBlank()) {
            return MatchResult.noMatch();
        }

        String normalizedInput = normalizeText(userInput);
        LOG.debugf("Matching scenario for input: %s", normalizedInput);

        // Get all scenarios from database
        List<CrisisScenario> scenarios = QuarkusTransaction.requiringNew().call(() ->
                scenarioRepository.findAllScenarios()
        );

        MatchResult bestMatch = MatchResult.noMatch();

        for (CrisisScenario scenario : scenarios) {
            List<String> keywords = parseKeywords(scenario.triggerKeywords);
            MatchResult result = checkScenarioMatch(scenario, keywords, normalizedInput);

            if (result.matched() && result.confidence() > bestMatch.confidence()) {
                bestMatch = result;
            }
        }

        if (bestMatch.matched()) {
            LOG.infof("Matched scenario: %s (keyword: %s, confidence: %.2f)",
                    bestMatch.scenario().scenarioKey, bestMatch.matchedKeyword(), bestMatch.confidence());
        } else {
            LOG.debug("No scenario matched");
        }

        return bestMatch;
    }

    /**
     * Check if a specific scenario matches the input.
     */
    private MatchResult checkScenarioMatch(CrisisScenario scenario, List<String> keywords, String normalizedInput) {
        int matchCount = 0;
        String firstMatchedKeyword = null;

        for (String keyword : keywords) {
            String normalizedKeyword = normalizeText(keyword);
            if (normalizedInput.contains(normalizedKeyword)) {
                matchCount++;
                if (firstMatchedKeyword == null) {
                    firstMatchedKeyword = keyword;
                }
            }
        }

        if (matchCount > 0) {
            // Calculate confidence based on number of matched keywords
            double confidence = Math.min(1.0, matchCount * 0.3 + 0.2);
            return MatchResult.matched(scenario, firstMatchedKeyword, confidence);
        }

        return MatchResult.noMatch();
    }

    /**
     * Parse trigger keywords JSON array.
     */
    private List<String> parseKeywords(String keywordsJson) {
        if (keywordsJson == null || keywordsJson.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(keywordsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            LOG.warnf("Failed to parse keywords JSON: %s", e.getMessage());
            return List.of();
        }
    }

    /**
     * Parse questions sequence JSON array.
     */
    public List<String> parseQuestions(String questionsJson) {
        if (questionsJson == null || questionsJson.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(questionsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            LOG.warnf("Failed to parse questions JSON: %s", e.getMessage());
            return List.of();
        }
    }

    /**
     * Normalize text for matching (lowercase, remove diacritics).
     */
    private String normalizeText(String text) {
        if (text == null) return "";

        // Lowercase and normalize Polish diacritics for better matching
        return text.toLowerCase()
                .replace("ą", "a")
                .replace("ć", "c")
                .replace("ę", "e")
                .replace("ł", "l")
                .replace("ń", "n")
                .replace("ó", "o")
                .replace("ś", "s")
                .replace("ź", "z")
                .replace("ż", "z")
                .trim();
    }

    /**
     * Get a scenario by key (for testing or direct lookup).
     */
    public Optional<CrisisScenario> getScenarioByKey(String key) {
        return QuarkusTransaction.requiringNew().call(() ->
                scenarioRepository.findByScenarioKey(key)
        );
    }
}
