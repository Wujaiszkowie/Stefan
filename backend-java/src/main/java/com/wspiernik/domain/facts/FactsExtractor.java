package com.wspiernik.domain.facts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wspiernik.infrastructure.llm.LlmClient;
import com.wspiernik.infrastructure.llm.PromptTemplates;
import com.wspiernik.infrastructure.persistence.entity.Fact;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts facts from conversation transcripts using LLM.
 * Task 24: Facts Extraction Logic
 */
@ApplicationScoped
public class FactsExtractor {

    private static final Logger LOG = Logger.getLogger(FactsExtractor.class);
    private static final double SIMILARITY_THRESHOLD = 0.8;
    private static final Pattern JSON_ARRAY_PATTERN = Pattern.compile("\\[.*?\\]", Pattern.DOTALL);

    @Inject
    LlmClient llmClient;

    @Inject
    PromptTemplates promptTemplates;

    @Inject
    ObjectMapper objectMapper;

    /**
     * Extract new facts from a conversation transcript.
     *
     * @param transcript     The raw conversation transcript
     * @param existingFacts  Already known facts (to avoid duplicates)
     * @return List of newly extracted facts
     */
    public List<ExtractedFact> extractFacts(String transcript, List<Fact> existingFacts) {
        if (transcript == null || transcript.isBlank()) {
            LOG.warn("Empty transcript provided for facts extraction");
            return List.of();
        }

        try {
            // Build the extraction prompt
            String prompt = promptTemplates.buildFactsDistillerPrompt(transcript, existingFacts);

            // Call LLM to extract facts
            LOG.debug("Calling LLM for facts extraction");
            String response = llmClient.generate("", prompt);

            // Parse JSON response
            List<ExtractedFact> extracted = parseFactsJson(response);

            // Filter out duplicates
            List<ExtractedFact> filtered = filterDuplicates(extracted, existingFacts);

            LOG.infof("Extracted %d facts, %d after filtering duplicates", extracted.size(), filtered.size());
            return filtered;

        } catch (Exception e) {
            LOG.errorf(e, "Failed to extract facts from transcript");
            return List.of();
        }
    }

    /**
     * Parse JSON response from LLM into ExtractedFact objects.
     */
    private List<ExtractedFact> parseFactsJson(String response) {
        if (response == null || response.isBlank()) {
            return List.of();
        }

        try {
            // Try to find JSON array in the response
            String jsonArray = extractJsonArray(response);
            if (jsonArray == null) {
                LOG.debug("No JSON array found in LLM response");
                return List.of();
            }

            List<ExtractedFact> facts = objectMapper.readValue(
                    jsonArray,
                    new TypeReference<List<ExtractedFact>>() {}
            );

            // Filter out invalid facts
            return facts.stream()
                    .filter(ExtractedFact::isValid)
                    .toList();

        } catch (Exception e) {
            LOG.warnf("Failed to parse facts JSON: %s. Response was: %s",
                    e.getMessage(), response.substring(0, Math.min(200, response.length())));
            return List.of();
        }
    }

    /**
     * Extract JSON array from LLM response (may contain extra text).
     */
    private String extractJsonArray(String response) {
        Matcher matcher = JSON_ARRAY_PATTERN.matcher(response);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * Filter out facts that are duplicates of existing facts.
     */
    private List<ExtractedFact> filterDuplicates(List<ExtractedFact> extracted, List<Fact> existingFacts) {
        if (existingFacts == null || existingFacts.isEmpty()) {
            return extracted;
        }

        List<ExtractedFact> unique = new ArrayList<>();
        for (ExtractedFact newFact : extracted) {
            boolean isDuplicate = false;
            for (Fact existing : existingFacts) {
                if (isSimilarFact(newFact, existing)) {
                    LOG.debugf("Skipping duplicate fact: %s (similar to existing: %s)",
                            newFact.value(), existing.factValue);
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) {
                unique.add(newFact);
            }
        }
        return unique;
    }

    /**
     * Check if two facts are similar (same type and high string similarity).
     */
    private boolean isSimilarFact(ExtractedFact newFact, Fact existing) {
        // Must be same type
        if (!newFact.type().equalsIgnoreCase(existing.factType)) {
            return false;
        }

        // Check string similarity of values
        String normalizedNew = normalizeForComparison(newFact.value());
        String normalizedExisting = normalizeForComparison(existing.factValue);

        return calculateSimilarity(normalizedNew, normalizedExisting) >= SIMILARITY_THRESHOLD;
    }

    /**
     * Normalize a string for comparison (lowercase, remove diacritics, trim).
     */
    private String normalizeForComparison(String s) {
        if (s == null) return "";
        String normalized = Normalizer.normalize(s.toLowerCase().trim(), Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", ""); // Remove diacritical marks
    }

    /**
     * Calculate Jaccard similarity between two strings (word-based).
     */
    private double calculateSimilarity(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;
        if (s1.isEmpty() || s2.isEmpty()) return 0.0;

        // Word-based Jaccard similarity
        String[] words1 = s1.split("\\s+");
        String[] words2 = s2.split("\\s+");

        java.util.Set<String> set1 = new java.util.HashSet<>(java.util.Arrays.asList(words1));
        java.util.Set<String> set2 = new java.util.HashSet<>(java.util.Arrays.asList(words2));

        java.util.Set<String> intersection = new java.util.HashSet<>(set1);
        intersection.retainAll(set2);

        java.util.Set<String> union = new java.util.HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }
}
