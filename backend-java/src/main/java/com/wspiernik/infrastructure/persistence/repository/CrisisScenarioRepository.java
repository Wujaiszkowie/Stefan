package com.wspiernik.infrastructure.persistence.repository;

import com.wspiernik.infrastructure.persistence.entity.CrisisScenario;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CrisisScenario entity operations.
 */
@ApplicationScoped
public class CrisisScenarioRepository implements PanacheRepository<CrisisScenario> {

    /**
     * Find scenario by key (fall, confusion, chest_pain).
     */
    public Optional<CrisisScenario> findByScenarioKey(String key) {
        return find("scenarioKey", key).firstResultOptional();
    }

    /**
     * Check if scenario with given key exists.
     */
    public boolean existsByScenarioKey(String key) {
        return count("scenarioKey", key) > 0;
    }

    /**
     * Get all scenarios.
     */
    public List<CrisisScenario> findAllScenarios() {
        return listAll();
    }

    /**
     * Check if any scenarios exist.
     */
    public boolean hasAnyScenarios() {
        return count() > 0;
    }
}
