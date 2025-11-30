package com.wspiernik.domain.facts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wspiernik.infrastructure.llm.LlmClient;
import com.wspiernik.infrastructure.llm.PromptTemplates;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedList;
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
    private static final Pattern JSON_ARRAY_PATTERN = Pattern.compile("\\{[^}]*}", Pattern.DOTALL);

    @Inject
    LlmClient llmClient;

    @Inject
    PromptTemplates promptTemplates;

    @Inject
    ObjectMapper objectMapper;

    /**
     * Extract new facts from a conversation transcript.
     *
     * @param transcript    The raw conversation transcript
     * @param existingFacts Already known facts (to avoid duplicates)
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
            LOG.debug("Calling LLM for facts extraction " + prompt);
            String response = llmClient.generate("", prompt);

            // Parse JSON response
            List<ExtractedFact> extracted = parseFactsJson(response);

            // Filter out duplicates
            //List<ExtractedFact> filtered = filterDuplicates(extracted, existingFacts);

            LOG.infof("Extracted %d facts, %d after filtering duplicates", extracted.size(), extracted.size());
            return extracted;

        } catch (Exception e) {
            LOG.errorf(e, "Failed to extract facts from transcript");
            return List.of();
        }
    }

    /**
     * Parse JSON response from LLM into ExtractedFact objects.
     */
    private List<ExtractedFact> parseFactsJson(String response) {

        LOG.debug("Parsing facts JSON from LLM response: \n" + response);
        if (response == null || response.isBlank()) {
            return List.of();
        }

        try {
            // Try to find JSON array in the response
            List<String> jsonArray = extractJsons(response);
            if (jsonArray.isEmpty()) {
                LOG.debug("No JSON array found in LLM response");
                return List.of();
            }
            List<ExtractedFact> facts = new LinkedList<>();
            for (String s : jsonArray) {
                try {
                    final var extractedFact = objectMapper.readValue(
                            s, ExtractedFact.class);
                    facts.add(extractedFact);

                } catch (Exception e) {
                    LOG.warnf(
                            "Failed to parse single fact JSON: %s. JSON was: %s",
                            e.getMessage(), s, e
                    );
                }

                // Filter out invalid facts


            }
            return facts.stream()
                    .filter(ExtractedFact::isValid)
                    .toList();
        } catch (Exception e) {
            LOG.warnf(
                    "Failed to parse facts JSON: %s. Response was: %s",
                    e.getMessage(), response, e
            );
            e.printStackTrace();
            return List.of();
        }
    }


    /**
     * Extract JSON array from LLM response (may contain extra text).
     */
    private List<String> extractJsons(String response) {

        Matcher matcher = JSON_ARRAY_PATTERN.matcher(response);
        List<String> facts = new LinkedList<>();
        while (matcher.find()) {
            facts.add(matcher.group());
        }
        return facts;
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
                    LOG.debugf(
                            "Skipping duplicate fact: %s (similar to existing: %s)",
                            newFact.value(), existing.factValue
                    );
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

        //TODO: to ma LLM robić
        return false;
    }

    /**
     * Normalize a string for comparison (lowercase, remove diacritics, trim).
     */
    private String normalizeForComparison(String s) {

        if (s == null) {
            return "";
        }
        String normalized = Normalizer.normalize(s.toLowerCase().trim(), Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", ""); // Remove diacritical marks
    }

}
