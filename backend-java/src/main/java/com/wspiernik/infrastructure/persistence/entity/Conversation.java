package com.wspiernik.infrastructure.persistence.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing a conversation session.
 * Stores the full transcript and metadata for survey, intervention, or support sessions.
 */
@Entity
@Table(name = "conversations")
public class Conversation extends PanacheEntity {

    @Column(name = "caregiver_id")
    public String caregiverId;

    @Column(name = "conversation_type")
    public String conversationType; // "survey", "intervention", "support"

    @Column(name = "scenario_type")
    public String scenarioType; // "fall", "confusion", "chest_pain" (nullable for support/survey)

    @Column(name = "raw_transcript", columnDefinition = "TEXT")
    public String rawTranscript;

    @Column(name = "started_at")
    public LocalDateTime startedAt;

    @Column(name = "ended_at")
    public LocalDateTime endedAt;

    @Column(name = "facts_extracted")
    public Boolean factsExtracted = false;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    /**
     * Find conversations by caregiver ID.
     */
    public static List<Conversation> findByCaregiverId(String caregiverId) {
        return list("caregiverId", caregiverId);
    }

    /**
     * Find conversations by type.
     */
    public static List<Conversation> findByConversationType(String type) {
        return list("conversationType", type);
    }

    /**
     * Find conversations that haven't been processed for facts extraction.
     */
    public static List<Conversation> findUnprocessedForFactsExtraction() {
        return list("factsExtracted", false);
    }
}
