package com.wspiernik.infrastructure.persistence.repository;

import com.wspiernik.infrastructure.persistence.entity.BackendLog;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for BackendLog entity operations.
 * Provides convenient logging methods.
 */
@ApplicationScoped
public class BackendLogRepository implements PanacheRepository<BackendLog> {

    /**
     * Log an INFO level message.
     */
    @Transactional
    public void logInfo(String module, String message) {
        logInfo(module, message, null);
    }

    /**
     * Log an INFO level message with details.
     */
    @Transactional
    public void logInfo(String module, String message, String details) {
        createLog("INFO", module, message, details);
    }

    /**
     * Log a WARNING level message.
     */
    @Transactional
    public void logWarning(String module, String message) {
        logWarning(module, message, null);
    }

    /**
     * Log a WARNING level message with details.
     */
    @Transactional
    public void logWarning(String module, String message, String details) {
        createLog("WARNING", module, message, details);
    }

    /**
     * Log an ERROR level message.
     */
    @Transactional
    public void logError(String module, String message) {
        logError(module, message, null);
    }

    /**
     * Log an ERROR level message with details.
     */
    @Transactional
    public void logError(String module, String message, String details) {
        createLog("ERROR", module, message, details);
    }

    /**
     * Find logs by level with limit.
     */
    public List<BackendLog> findByLevel(String level, int limit) {
        return find("level = ?1 ORDER BY timestamp DESC", level).page(0, limit).list();
    }

    /**
     * Find recent logs with limit.
     */
    public List<BackendLog> findRecent(int limit) {
        return find("ORDER BY timestamp DESC").page(0, limit).list();
    }

    /**
     * Find logs by module with limit.
     */
    public List<BackendLog> findByModule(String module, int limit) {
        return find("module = ?1 ORDER BY timestamp DESC", module).page(0, limit).list();
    }

    private void createLog(String level, String module, String message, String details) {
        BackendLog log = new BackendLog();
        log.timestamp = LocalDateTime.now();
        log.level = level;
        log.module = module;
        log.message = message;
        log.details = details;
        persist(log);
    }
}
