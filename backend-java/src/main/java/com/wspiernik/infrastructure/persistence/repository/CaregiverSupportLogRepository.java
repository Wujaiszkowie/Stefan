package com.wspiernik.infrastructure.persistence.repository;

import com.wspiernik.infrastructure.persistence.entity.CaregiverSupportLog;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CaregiverSupportLog entity operations.
 */
@ApplicationScoped
public class CaregiverSupportLogRepository implements PanacheRepository<CaregiverSupportLog> {

    /**
     * Find support logs by caregiver ID.
     */
    public List<CaregiverSupportLog> findByCaregiverId(String caregiverId) {
        return list("caregiverId", caregiverId);
    }

    /**
     * Find support log by conversation ID.
     */
    public Optional<CaregiverSupportLog> findByConversationId(Long conversationId) {
        return find("conversationId", conversationId).firstResultOptional();
    }

    /**
     * Find recent support logs with limit.
     */
    public List<CaregiverSupportLog> findRecent(int limit) {
        return find("ORDER BY createdAt DESC").page(0, limit).list();
    }

    /**
     * Calculate average stress level for a caregiver.
     */
    public Double averageStressLevel(String caregiverId) {
        List<CaregiverSupportLog> logs = findByCaregiverId(caregiverId);
        if (logs.isEmpty()) {
            return null;
        }
        return logs.stream()
                .filter(log -> log.stressLevel != null)
                .mapToInt(log -> log.stressLevel)
                .average()
                .orElse(0.0);
    }
}
