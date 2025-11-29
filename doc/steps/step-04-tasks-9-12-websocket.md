# Step 04: WebSocket Infrastructure (Tasks 9-12)

**Status:** DONE
**Tasks:** 9, 10, 11, 12

---

## Task 9: WebSocket DTOs

### Goal
Define all WebSocket message types as Java records.

### Changes
- **New Files:**
  - `src/main/java/com/wspiernik/api/dto/IncomingMessage.java`
  - `src/main/java/com/wspiernik/api/dto/OutgoingMessage.java`
  - `src/main/java/com/wspiernik/api/dto/payload/*.java` (specific payloads)

### Message Format (from PRD)
```json
{
  "type": "string",
  "payload": {},
  "request_id": "string"
}
```

### Incoming Message Types
| Type | Payload | Description |
|------|---------|-------------|
| `survey_start` | - | Start survey |
| `survey_message` | `{text}` | Survey answer |
| `survey_complete` | - | End survey |
| `intervention_start` | `{scenario_description}` | Start intervention |
| `intervention_message` | `{text}` | Intervention answer |
| `intervention_complete` | - | End intervention |
| `support_start` | - | Start support |
| `support_message` | `{text}` | Support message |
| `support_complete` | - | End support |
| `get_facts` | `{limit}` | Query facts |
| `get_profile` | - | Query profile |

### Outgoing Message Types
| Type | Payload | Description |
|------|---------|-------------|
| `survey_question` | `{question, step}` | Survey question |
| `survey_completed` | `{profile_id, facts_saved}` | Survey done |
| `intervention_scenario_matched` | `{scenario_key, scenario_name}` | Scenario found |
| `intervention_question` | `{question, step}` | Intervention question |
| `intervention_completed` | `{conversation_id, facts_extraction}` | Intervention done |
| `support_message` | `{text}` | Support response |
| `support_completed` | `{conversation_id, facts_extraction}` | Support done |
| `facts_extracted` | `{conversation_id, facts_count, facts}` | Async notification |
| `facts_list` | `{facts, total_count}` | Facts query result |
| `profile_data` | `{ward_age, ward_conditions, ...}` | Profile data |
| `error` | `{message, code}` | Error response |

### Acceptance Criteria
- [x] All DTOs defined as Java records
- [x] Jackson annotations for JSON serialization
- [x] Payload types match PRD specification

---

## Task 10: WebSocket Endpoint

### Goal
Create the main WebSocket endpoint using Quarkus WebSockets Next.

### Changes
- **New File:**
  - `src/main/java/com/wspiernik/api/websocket/WspiernikSocket.java`

### Implementation
```java
@WebSocket(path = "/ws")
public class WspiernikSocket {

    @OnOpen
    void onOpen(WebSocketConnection connection) { }

    @OnTextMessage
    void onMessage(String message, WebSocketConnection connection) { }

    @OnClose
    void onClose(WebSocketConnection connection) { }

    @OnError
    void onError(WebSocketConnection connection, Throwable error) { }
}
```

### Features
- Parse incoming JSON to `IncomingMessage`
- Delegate to `MessageDispatcher`
- Maintain connection registry for async notifications
- Handle malformed messages gracefully

### Acceptance Criteria
- [x] WebSocket endpoint accessible at `/ws`
- [x] JSON parsing works correctly
- [x] Connections tracked for broadcast capability

---

## Task 11: Message Dispatcher

### Goal
Route incoming messages to appropriate service handlers.

### Changes
- **New File:**
  - `src/main/java/com/wspiernik/api/websocket/MessageDispatcher.java`

### Routing Table
```java
switch (message.type()) {
    case "survey_start" -> surveyHandler.start(connection, message);
    case "survey_message" -> surveyHandler.message(connection, message);
    case "survey_complete" -> surveyHandler.complete(connection, message);
    case "intervention_start" -> interventionHandler.start(connection, message);
    case "intervention_message" -> interventionHandler.message(connection, message);
    case "intervention_complete" -> interventionHandler.complete(connection, message);
    case "support_start" -> supportHandler.start(connection, message);
    case "support_message" -> supportHandler.message(connection, message);
    case "support_complete" -> supportHandler.complete(connection, message);
    case "get_facts" -> queryHandler.getFacts(connection, message);
    case "get_profile" -> queryHandler.getProfile(connection, message);
    default -> sendError(connection, "Unknown message type");
}
```

### Acceptance Criteria
- [x] All message types routed correctly
- [x] Unknown types return error response
- [x] Handlers are injectable services

---

## Task 12: Session Manager

### Goal
Track active conversation state per WebSocket connection.

### Changes
- **New File:**
  - `src/main/java/com/wspiernik/api/websocket/ConversationSessionManager.java`

### Session Data
```java
record ConversationSession(
    String sessionId,
    String caregiverId,
    String conversationType,  // "survey", "intervention", "support"
    Long conversationId,
    int currentStep,
    List<LlmMessage> messageHistory,
    Map<String, Object> context
) {}
```

### Methods
```java
void startSession(WebSocketConnection conn, String type);
ConversationSession getSession(WebSocketConnection conn);
void updateSession(WebSocketConnection conn, Consumer<ConversationSession> updater);
void endSession(WebSocketConnection conn);
boolean hasActiveSession(WebSocketConnection conn);
```

### Implementation Details
- Use `ConcurrentHashMap` for thread safety
- Session tied to WebSocket connection ID
- Clean up on connection close
- Support for storing arbitrary context (profile, scenario, etc.)

### Acceptance Criteria
- [x] Sessions created/retrieved correctly
- [x] Thread-safe implementation
- [x] Sessions cleaned up on disconnect

---

## Implementation Notes

- Using Quarkus WebSockets Next (not Jakarta WebSocket)
- Single WebSocket endpoint at `/ws`
- All message handling is asynchronous
- Connection registry needed for `facts_extracted` notifications
