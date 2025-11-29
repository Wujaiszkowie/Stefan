package com.wspiernik.infrastructure.persistence.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entity representing caregiver and ward (patient) profile information.
 * Stores baseline health data collected during the initial survey.
 */
@Entity
@Table(name = "caregiver_profile")
public class CaregiverProfile extends PanacheEntity {

    @Column(name = "caregiver_id", unique = true)
    public String caregiverId;

    @Column(name = "ward_age")
    public Integer wardAge;

    @Column(name = "ward_conditions", columnDefinition = "TEXT")
    public String wardConditions; // JSON array: ["hypertension", "diabetes", ...]

    @Column(name = "ward_medications", columnDefinition = "TEXT")
    public String wardMedications; // JSON array: [{name, dose, frequency}, ...]

    @Column(name = "ward_mobility_limits", columnDefinition = "TEXT")
    public String wardMobilityLimits; // JSON

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    /**
     * Find profile by caregiver ID.
     */
    public static CaregiverProfile findByCaregiverId(String caregiverId) {
        return find("caregiverId", caregiverId).firstResult();
    }
}
