# Step 09: Logging & Error Handling (Tasks 26-28)

**Status:** PENDING
**Tasks:** 26, 27, 28

---

## Task 26: Custom SQLite Logger

### Goal
Create a logging service that persists logs to SQLite database.

### Changes
- **New File:**
  - `src/main/java/com/wspiernik/infrastructure/logging/SqliteLogger.java`

### Logger Interface
```java
@ApplicationScoped
public class SqliteLogger {

    void info(String module, String message);
    void info(String module, String message, Object details);
    void warning(String module, String message);
    void warning(String module, String message, Object details);
    void error(String module, String message);
    void error(String module, String message, Throwable exception);
    void error(String module, String message, Object details);
}
```

### Implementation
```java
private void log(String level, String module, String message, Object details) {
    BackendLog log = new BackendLog();
    log.timestamp = LocalDateTime.now();
    log.level = level;
    log.module = module;
    log.message = message;
    log.details = details != null ? toJson(details) : null;
    log.persist();

    // Also log to console for debugging
    switch (level) {
        case "ERROR" -> logger.error("[{}] {}", module, message);
        case "WARNING" -> logger.warn("[{}] {}", module, message);
        default -> logger.info("[{}] {}", module, message);
    }
}
```

### Module Names
| Module | Description |
|--------|-------------|
| `WEBSOCKET` | WebSocket events |
| `SURVEY` | Survey module |
| `INTERVENTION` | Intervention module |
| `SUPPORT` | Support module |
| `FACTS` | Facts Distiller |
| `LLM` | LLM interactions |
| `DATABASE` | Database operations |
| `STARTUP` | Application lifecycle |

### Log Details Format
```json
{
  "sessionId": "abc123",
  "caregiverId": "user_001",
  "conversationType": "intervention",
  "errorCode": "LLM_TIMEOUT",
  "stackTrace": "..."
}
```

### Acceptance Criteria
- [ ] Logs written to `backend_logs` table
- [ ] Also outputs to console
- [ ] Details stored as JSON
- [ ] All modules use consistent logging

---

## Task 27: Global Error Handler

### Goal
Implement centralized error handling for WebSocket.

### Changes
- **New File:**
  - `src/main/java/com/wspiernik/api/websocket/WebSocketErrorHandler.java`

### Error Types
| Error | Code | User Message |
|-------|------|--------------|
| JSON Parse Error | `PARSE_ERROR` | "Nieprawidłowy format wiadomości" |
| Unknown Message Type | `UNKNOWN_TYPE` | "Nieznany typ wiadomości" |
| LLM Timeout | `LLM_TIMEOUT` | "Przepraszam, odpowiedź trwa dłużej niż zwykle. Spróbuj ponownie." |
| LLM Error | `LLM_ERROR` | "Wystąpił problem z generowaniem odpowiedzi" |
| Database Error | `DB_ERROR` | "Wystąpił problem z zapisem danych" |
| Session Not Found | `SESSION_ERROR` | "Sesja nie istnieje. Rozpocznij nową rozmowę." |
| Internal Error | `INTERNAL_ERROR` | "Wystąpił nieoczekiwany błąd" |

### Error Response Format
```json
{
  "type": "error",
  "payload": {
    "code": "LLM_TIMEOUT",
    "message": "Przepraszam, odpowiedź trwa dłużej niż zwykle. Spróbuj ponownie.",
    "request_id": "req_123"
  }
}
```

### Handler Implementation
```java
@ApplicationScoped
public class WebSocketErrorHandler {

    void handleError(WebSocketConnection connection, Throwable error, String requestId) {
        ErrorCode code = classifyError(error);
        String userMessage = getUserMessage(code);

        // Log to database
        sqliteLogger.error("WEBSOCKET", error.getMessage(), Map.of(
            "errorCode", code,
            "requestId", requestId,
            "stackTrace", getStackTrace(error)
        ));

        // Send error response
        sendErrorResponse(connection, code, userMessage, requestId);
    }

    private ErrorCode classifyError(Throwable error) {
        if (error instanceof JsonParseException) return ErrorCode.PARSE_ERROR;
        if (error instanceof TimeoutException) return ErrorCode.LLM_TIMEOUT;
        if (error instanceof LlmException) return ErrorCode.LLM_ERROR;
        if (error instanceof PersistenceException) return ErrorCode.DB_ERROR;
        return ErrorCode.INTERNAL_ERROR;
    }
}
```

### Integration with WebSocket
```java
@OnTextMessage
void onMessage(String message, WebSocketConnection connection) {
    try {
        IncomingMessage msg = parseMessage(message);
        dispatcher.dispatch(msg, connection);
    } catch (Exception e) {
        errorHandler.handleError(connection, e, extractRequestId(message));
    }
}
```

### Acceptance Criteria
- [ ] All errors logged to database
- [ ] User-friendly messages in Polish
- [ ] Error codes for debugging
- [ ] No stack traces sent to client

---

## Task 28: Integration & Cleanup

### Goal
Final integration, cleanup, and health check.

### Changes
- **Delete:** `src/main/java/org/acme/GreetingResource.java`
- **Delete:** `src/test/java/org/acme/GreetingResourceTest.java`
- **Delete:** `src/test/java/org/acme/GreetingResourceIT.java`
- **New File:** `src/main/java/com/wspiernik/api/rest/HealthResource.java`

### Health Check Endpoint
```java
@Path("/api")
public class HealthResource {

    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    public HealthStatus health() {
        return new HealthStatus(
            "UP",
            LocalDateTime.now(),
            checkDatabase(),
            checkLlm()
        );
    }
}

record HealthStatus(
    String status,
    LocalDateTime timestamp,
    ComponentStatus database,
    ComponentStatus llm
) {}

record ComponentStatus(
    String name,
    String status,
    String message
) {}
```

### Health Response
```json
{
  "status": "UP",
  "timestamp": "2025-11-29T12:00:00",
  "database": {
    "name": "SQLite",
    "status": "UP",
    "message": "Connected"
  },
  "llm": {
    "name": "Bielnik",
    "status": "UP",
    "message": "Responding"
  }
}
```

### Final Checklist
- [ ] Old `org.acme` package removed
- [ ] All `com.wspiernik` packages created
- [ ] Health endpoint working
- [ ] Application starts without errors
- [ ] WebSocket endpoint accessible
- [ ] Database file created
- [ ] Scenarios seeded

### Smoke Test
```bash
# Start application
./mvnw quarkus:dev

# Check health
curl http://localhost:8080/api/health

# Connect WebSocket
wscat -c ws://localhost:8080/ws

# Send test message
{"type": "get_profile", "request_id": "test_1"}
```

### Acceptance Criteria
- [ ] Clean project structure
- [ ] All endpoints accessible
- [ ] No compilation errors
- [ ] Basic smoke test passes

---

## Implementation Notes

- Error messages in Polish for user-facing responses
- Technical details only in logs, not in client responses
- Health check useful for Docker/monitoring
- Logging to both console and database
