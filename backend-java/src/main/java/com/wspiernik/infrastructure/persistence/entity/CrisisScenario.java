package com.wspiernik.infrastructure.persistence.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Entity representing a predefined crisis scenario.
 * Contains trigger keywords and question sequences for handling specific crisis situations.
 */
@Entity
@Table(name = "crisis_scenarios")
public class CrisisScenario extends PanacheEntity {

    @Column(name = "scenario_key", unique = true)
    public String scenarioKey; // "fall", "confusion", "chest_pain"

    @Column(name = "name")
    public String name; // Display name: "Upadek", "Zamieszanie Umysłowe", etc.

    @Column(name = "trigger_keywords", columnDefinition = "TEXT")
    public String triggerKeywords; // JSON array of trigger words

    @Column(name = "questions_sequence", columnDefinition = "TEXT")
    public String questionsSequence; // JSON array of questions

    @Column(name = "system_prompt", columnDefinition = "TEXT")
    public String systemPrompt; // LLM system prompt for this scenario

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    /**
     * Find scenario by key.
     */
    public static Optional<CrisisScenario> findByScenarioKey(String key) {
        return find("scenarioKey", key).firstResultOptional();
    }

    /**
     * Check if scenario with given key exists.
     */
    public static boolean existsByScenarioKey(String key) {
        return count("scenarioKey", key) > 0;
    }
}
