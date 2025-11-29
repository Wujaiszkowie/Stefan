package com.wspiernik.infrastructure.persistence.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing a log entry for caregiver support sessions.
 * Tracks stress levels and needs identified during support conversations.
 * Used for Phase 2 periodic check-ins feature.
 */
@Entity
@Table(name = "caregiver_support_logs")
public class CaregiverSupportLog extends PanacheEntity {

    @Column(name = "caregiver_id")
    public String caregiverId;

    @Column(name = "conversation_id")
    public Long conversationId;

    @Column(name = "stress_level")
    public Integer stressLevel; // 1-10 (extracted from conversation)

    @Column(name = "needs", columnDefinition = "TEXT")
    public String needs; // JSON array of identified needs

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    /**
     * Find support logs by caregiver ID.
     */
    public static List<CaregiverSupportLog> findByCaregiverId(String caregiverId) {
        return list("caregiverId", caregiverId);
    }

    /**
     * Find support log by conversation ID.
     */
    public static CaregiverSupportLog findByConversationId(Long conversationId) {
        return find("conversationId", conversationId).firstResult();
    }
}
