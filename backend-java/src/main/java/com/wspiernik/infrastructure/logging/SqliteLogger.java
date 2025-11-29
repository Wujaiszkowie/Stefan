package com.wspiernik.infrastructure.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wspiernik.infrastructure.persistence.entity.BackendLog;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Custom logger that persists logs to SQLite database.
 * Also outputs to console for debugging.
 * Task 26: Custom SQLite Logger
 */
@ApplicationScoped
public class SqliteLogger {

    private static final Logger LOG = Logger.getLogger(SqliteLogger.class);

    // Module constants
    public static final String MODULE_WEBSOCKET = "WEBSOCKET";
    public static final String MODULE_SURVEY = "SURVEY";
    public static final String MODULE_INTERVENTION = "INTERVENTION";
    public static final String MODULE_SUPPORT = "SUPPORT";
    public static final String MODULE_FACTS = "FACTS";
    public static final String MODULE_LLM = "LLM";
    public static final String MODULE_DATABASE = "DATABASE";
    public static final String MODULE_STARTUP = "STARTUP";

    @Inject
    ObjectMapper objectMapper;

    // =========================================================================
    // INFO Level
    // =========================================================================

    public void info(String module, String message) {
        log("INFO", module, message, null);
    }

    public void info(String module, String message, Object details) {
        log("INFO", module, message, details);
    }

    public void info(String module, String message, Map<String, Object> details) {
        log("INFO", module, message, details);
    }

    // =========================================================================
    // WARNING Level
    // =========================================================================

    public void warning(String module, String message) {
        log("WARNING", module, message, null);
    }

    public void warning(String module, String message, Object details) {
        log("WARNING", module, message, details);
    }

    public void warning(String module, String message, Map<String, Object> details) {
        log("WARNING", module, message, details);
    }

    // =========================================================================
    // ERROR Level
    // =========================================================================

    public void error(String module, String message) {
        log("ERROR", module, message, null);
    }

    public void error(String module, String message, Throwable exception) {
        Map<String, Object> details = Map.of(
                "exceptionClass", exception.getClass().getName(),
                "exceptionMessage", exception.getMessage() != null ? exception.getMessage() : "",
                "stackTrace", getStackTrace(exception)
        );
        log("ERROR", module, message, details);
    }

    public void error(String module, String message, Object details) {
        log("ERROR", module, message, details);
    }

    public void error(String module, String message, Map<String, Object> details) {
        log("ERROR", module, message, details);
    }

    // =========================================================================
    // Core Logging
    // =========================================================================

    private void log(String level, String module, String message, Object details) {
        // Log to console first (always works)
        logToConsole(level, module, message);

        // Persist to database asynchronously (don't block caller)
        try {
            String detailsJson = details != null ? toJson(details) : null;
            persistLog(level, module, message, detailsJson);
        } catch (Exception e) {
            // Don't fail if database logging fails
            LOG.warnf("Failed to persist log to database: %s", e.getMessage());
        }
    }

    private void logToConsole(String level, String module, String message) {
        String formattedMessage = String.format("[%s] %s", module, message);
        switch (level) {
            case "ERROR" -> LOG.error(formattedMessage);
            case "WARNING" -> LOG.warn(formattedMessage);
            default -> LOG.info(formattedMessage);
        }
    }

    private void persistLog(String level, String module, String message, String detailsJson) {
        try {
            QuarkusTransaction.requiringNew().run(() -> {
                BackendLog log = new BackendLog();
                log.timestamp = LocalDateTime.now();
                log.level = level;
                log.module = module;
                log.message = message;
                log.details = detailsJson;
                log.persist();
            });
        } catch (Exception e) {
            // Silently ignore database errors to prevent cascading failures
            LOG.debugf("Could not persist log: %s", e.getMessage());
        }
    }

    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
