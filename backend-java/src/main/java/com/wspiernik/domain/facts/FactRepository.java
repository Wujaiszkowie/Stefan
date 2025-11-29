package com.wspiernik.domain.facts;

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
     * Find all facts (no limit).
     */
    public List<Fact> findAllFacts() {
        return find("ORDER BY createdAt DESC").list();
    }

    /**
     * Check if any facts exist.
     */
    public boolean hasAnyFacts() {
        return count() > 0;
    }
}
