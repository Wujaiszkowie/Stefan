package com.wspiernik.api.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wspiernik.api.websocket.dto.ErrorPayload;
import com.wspiernik.api.websocket.dto.OutgoingMessage;
import com.wspiernik.infrastructure.logging.SqliteLogger;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import org.jboss.logging.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Centralized error handling for WebSocket connections.
 * Task 27: Global Error Handler
 */
@ApplicationScoped
public class WebSocketErrorHandler {

    private static final Logger LOG = Logger.getLogger(WebSocketErrorHandler.class);

    // Error codes
    public static final String CODE_PARSE_ERROR = "PARSE_ERROR";
    public static final String CODE_UNKNOWN_TYPE = "UNKNOWN_TYPE";
    public static final String CODE_LLM_TIMEOUT = "LLM_TIMEOUT";
    public static final String CODE_LLM_ERROR = "LLM_ERROR";
    public static final String CODE_DB_ERROR = "DB_ERROR";
    public static final String CODE_SESSION_ERROR = "SESSION_ERROR";
    public static final String CODE_INTERNAL_ERROR = "INTERNAL_ERROR";

    // User-friendly messages in Polish
    private static final Map<String, String> USER_MESSAGES = Map.of(
            CODE_PARSE_ERROR, "Nieprawidłowy format wiadomości",
            CODE_UNKNOWN_TYPE, "Nieznany typ wiadomości",
            CODE_LLM_TIMEOUT, "Przepraszam, odpowiedź trwa dłużej niż zwykle. Spróbuj ponownie.",
            CODE_LLM_ERROR, "Wystąpił problem z generowaniem odpowiedzi",
            CODE_DB_ERROR, "Wystąpił problem z zapisem danych",
            CODE_SESSION_ERROR, "Sesja nie istnieje. Rozpocznij nową rozmowę.",
            CODE_INTERNAL_ERROR, "Wystąpił nieoczekiwany błąd"
    );

    @Inject
    SqliteLogger sqliteLogger;

    @Inject
    MessageSender messageSender;

    /**
     * Handle an error and send appropriate response to client.
     */
    public void handleError(WebSocketConnection connection, Throwable error, String requestId) {
        String errorCode = classifyError(error);
        String userMessage = getUserMessage(errorCode);

        // Log to database with details
        logError(connection, error, errorCode, requestId);

        // Send error response to client (no stack trace)
        sendErrorResponse(connection, errorCode, userMessage, requestId);
    }

    /**
     * Handle an error with a specific error code.
     */
    public void handleError(WebSocketConnection connection, String errorCode, String customMessage, String requestId) {
        String userMessage = customMessage != null ? customMessage : getUserMessage(errorCode);

        // Log to database
        sqliteLogger.error(SqliteLogger.MODULE_WEBSOCKET, userMessage, Map.of(
                "errorCode", errorCode,
                "connectionId", connection.id(),
                "requestId", requestId != null ? requestId : ""
        ));

        // Send error response
        sendErrorResponse(connection, errorCode, userMessage, requestId);
    }

    /**
     * Classify error into error code.
     */
    private String classifyError(Throwable error) {
        if (error instanceof JsonProcessingException) {
            return CODE_PARSE_ERROR;
        }
        if (error instanceof TimeoutException) {
            return CODE_LLM_TIMEOUT;
        }
        if (error instanceof PersistenceException) {
            return CODE_DB_ERROR;
        }
        if (error.getClass().getName().contains("LlmException") ||
            error.getMessage() != null && error.getMessage().contains("LLM")) {
            return CODE_LLM_ERROR;
        }
        if (error instanceof IllegalStateException &&
            error.getMessage() != null && error.getMessage().contains("session")) {
            return CODE_SESSION_ERROR;
        }
        return CODE_INTERNAL_ERROR;
    }

    /**
     * Get user-friendly message for error code.
     */
    public String getUserMessage(String errorCode) {
        return USER_MESSAGES.getOrDefault(errorCode, USER_MESSAGES.get(CODE_INTERNAL_ERROR));
    }

    /**
     * Log error to database with full details.
     */
    private void logError(WebSocketConnection connection, Throwable error, String errorCode, String requestId) {
        Map<String, Object> details = new HashMap<>();
        details.put("errorCode", errorCode);
        details.put("connectionId", connection.id());
        details.put("requestId", requestId != null ? requestId : "");
        details.put("exceptionClass", error.getClass().getName());
        details.put("exceptionMessage", error.getMessage() != null ? error.getMessage() : "");
        details.put("stackTrace", getStackTrace(error));

        sqliteLogger.error(SqliteLogger.MODULE_WEBSOCKET, error.getMessage(), details);
    }

    /**
     * Send error response to client.
     */
    private void sendErrorResponse(WebSocketConnection connection, String errorCode, String userMessage, String requestId) {
        try {
            OutgoingMessage errorMessage = OutgoingMessage.error(userMessage, errorCode, requestId);
            messageSender.send(connection, errorMessage);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to send error response to %s", connection.id());
        }
    }

    /**
     * Get stack trace as string.
     */
    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
