package com.wspiernik.infrastructure.persistence.repository;

import com.wspiernik.infrastructure.persistence.entity.Conversation;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Repository for Conversation entity operations.
 */
@ApplicationScoped
public class ConversationRepository implements PanacheRepository<Conversation> {

    /**
     * Find conversations by caregiver ID.
     */
    public List<Conversation> findByCaregiverId(String caregiverId) {
        return list("caregiverId", caregiverId);
    }

    /**
     * Find conversations by type (survey, intervention, support).
     */
    public List<Conversation> findByConversationType(String type) {
        return list("conversationType", type);
    }

    /**
     * Find conversations that haven't been processed for facts extraction.
     */
    public List<Conversation> findUnprocessedForFactsExtraction() {
        return list("factsExtracted", false);
    }

    /**
     * Find recent conversations with limit.
     */
    public List<Conversation> findRecent(int limit) {
        return find("ORDER BY createdAt DESC").page(0, limit).list();
    }

    /**
     * Mark a conversation as having facts extracted.
     */
    public void markFactsExtracted(Long conversationId) {
        update("factsExtracted = true WHERE id = ?1", conversationId);
    }
}
