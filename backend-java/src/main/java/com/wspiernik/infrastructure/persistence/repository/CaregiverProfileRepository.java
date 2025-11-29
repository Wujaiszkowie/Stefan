package com.wspiernik.infrastructure.persistence.repository;

import com.wspiernik.infrastructure.persistence.entity.CaregiverProfile;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

/**
 * Repository for CaregiverProfile entity operations.
 */
@ApplicationScoped
public class CaregiverProfileRepository implements PanacheRepository<CaregiverProfile> {

    /**
     * Find profile by caregiver ID.
     */
    public Optional<CaregiverProfile> findByCaregiverId(String caregiverId) {
        return find("caregiverId", caregiverId).firstResultOptional();
    }

    /**
     * Check if a profile exists for the given caregiver ID.
     */
    public boolean existsByCaregiverId(String caregiverId) {
        return count("caregiverId", caregiverId) > 0;
    }

    /**
     * Get the first (and typically only) profile.
     * Used in MVP where there's only one caregiver.
     */
    public Optional<CaregiverProfile> findFirst() {
        return findAll().firstResultOptional();
    }
}
