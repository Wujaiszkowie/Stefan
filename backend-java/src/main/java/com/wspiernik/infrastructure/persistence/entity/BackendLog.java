package com.wspiernik.infrastructure.persistence.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing backend application logs.
 * Stores INFO, WARNING, and ERROR level logs with details for debugging.
 */
@Entity
@Table(name = "backend_logs")
public class BackendLog extends PanacheEntity {

    @Column(name = "timestamp")
    public LocalDateTime timestamp;

    @Column(name = "level")
    public String level; // "INFO", "WARNING", "ERROR"

    @Column(name = "module")
    public String module; // Module name (e.g., "WEBSOCKET", "LLM", "SURVEY")

    @Column(name = "message", columnDefinition = "TEXT")
    public String message;

    @Column(name = "details", columnDefinition = "TEXT")
    public String details; // JSON with additional details

    /**
     * Find logs by level with limit.
     */
    public static List<BackendLog> findByLevel(String level, int limit) {
        return find("level = ?1 ORDER BY timestamp DESC", level).page(0, limit).list();
    }

    /**
     * Find recent logs with limit.
     */
    public static List<BackendLog> findRecent(int limit) {
        return find("ORDER BY timestamp DESC").page(0, limit).list();
    }

    /**
     * Find logs by module with limit.
     */
    public static List<BackendLog> findByModule(String module, int limit) {
        return find("module = ?1 ORDER BY timestamp DESC", module).page(0, limit).list();
    }
}
