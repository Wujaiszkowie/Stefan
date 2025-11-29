package com.wspiernik.infrastructure.persistence.repository;

import com.wspiernik.infrastructure.persistence.entity.Fact;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Repository for Fact entity operations.
 */
@ApplicationScoped
public class FactRepository implements PanacheRepository<Fact> {

    /**
     * Find facts by conversation ID.
     */
    public List<Fact> findByConversationId(Long conversationId) {
        return list("conversationId", conversationId);
    }

    /**
     * Find facts by type (symptom, medication, event, condition, limitation).
     */
    public List<Fact> findByFactType(String factType) {
        return list("factType", factType);
    }

    /**
     * Find all facts ordered by creation date (newest first), with limit.
     */
    public List<Fact> findAllOrderByCreatedAtDesc(int limit) {
        return find("ORDER BY createdAt DESC").page(0, limit).list();
    }

    /**
     * Find recent facts (alias for findAllOrderByCreatedAtDesc).
     */
    public List<Fact> findRecentFacts(int limit) {
        return findAllOrderByCreatedAtDesc(limit);
    }

    /**
     * Find all facts (no limit).
     */
    public List<Fact> findAllFacts() {
        return find("ORDER BY createdAt DESC").list();
    }

    /**
     * Count all facts.
     */
    public long countAllFacts() {
        return count();
    }

    /**
     * Check if any facts exist.
     */
    public boolean hasAnyFacts() {
        return count() > 0;
    }
}
