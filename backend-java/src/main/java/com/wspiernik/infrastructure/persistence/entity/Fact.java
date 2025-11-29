package com.wspiernik.infrastructure.persistence.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing an extracted fact from a conversation.
 * Facts are structured pieces of health information (symptoms, medications, events, etc.).
 */
@Entity
@Table(name = "facts")
public class Fact extends PanacheEntity {

    @Column(name = "conversation_id")
    public Long conversationId;

    @Column(name = "fact_type")
    public String factType; // "symptom", "medication", "event", "condition", "limitation"

    @Column(name = "fact_value", columnDefinition = "TEXT")
    public String factValue;

    @Column(name = "severity")
    public Integer severity; // 1-10 (nullable)

    @Column(name = "context", columnDefinition = "TEXT")
    public String context; // Additional context

    @Column(name = "extracted_at")
    public LocalDateTime extractedAt;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    /**
     * Find facts by conversation ID.
     */
    public static List<Fact> findByConversationId(Long conversationId) {
        return list("conversationId", conversationId);
    }

    /**
     * Find facts by type.
     */
    public static List<Fact> findByFactType(String factType) {
        return list("factType", factType);
    }

    /**
     * Find all facts ordered by creation date (newest first), with limit.
     */
    public static List<Fact> findAllOrderByCreatedAtDesc(int limit) {
        return find("ORDER BY createdAt DESC").page(0, limit).list();
    }

    /**
     * Count all facts.
     */
    public static long countAll() {
        return count();
    }
}
