# Wspiernik - Backend

Quarkus-based backend for the Wspiernik caregiver support system.

## Tech Stack

- **Quarkus 3.30** - Supersonic Subatomic Java
- **Java 21** - LTS version
- **SQLite** - Embedded database
- **Hibernate ORM Panache** - Simplified ORM
- **WebSocket** - Real-time communication
- **Bielik LLM** - Polish language model integration

## Project Structure

```
src/main/java/com/wspiernik/
├── api/
│   ├── rest/
│   │   ├── FactResource.java      # Facts REST endpoint
│   │   ├── HealthResource.java    # Health check
│   │   └── LlmTestResource.java   # LLM testing endpoint
│   └── websocket/
│       ├── WspiernikSocket.java   # WebSocket endpoint
│       ├── MessageDispatcher.java # Message routing
│       ├── MessageSender.java     # Outgoing messages
│       ├── ConversationSessionManager.java
│       ├── dto/                   # WebSocket DTOs
│       │   ├── IncomingMessage.java
│       │   ├── OutgoingMessage.java
│       │   ├── FactDto.java
│       │   └── ...
│       └── handler/               # Message handlers
│           ├── SurveyHandler.java
│           ├── SupportHandler.java
│           ├── InterventionHandler.java
│           └── QueryHandler.java
├── domain/
│   ├── facts/
│   │   ├── Fact.java              # Fact entity
│   │   └── FactRepository.java    # Fact repository
│   ├── conversation/
│   │   └── Conversation.java
│   └── intervention/
│       └── InterventionService.java
└── infrastructure/
    └── llm/
        ├── LlmClient.java         # LLM interface
        ├── BielnikLlmClient.java  # Bielik implementation
        ├── MockLlmClient.java     # Mock for testing
        └── dto/
            ├── LlmRequest.java
            └── LlmResponse.java
```

## Features

- **WebSocket API** - Real-time bidirectional communication
- **Conversation Types**:
  - `survey` - Collect patient information
  - `support` - Mental health support chat
  - `intervention` - Emergency intervention flow
- **Facts Extraction** - Extract structured data from conversations
- **LLM Integration** - Polish Bielik model for AI responses

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+

### Development

```bash
./mvnw quarkus:dev
```

Opens http://localhost:8080

Dev UI available at http://localhost:8080/q/dev/

### Development with Mock LLM

```bash
./mvnw quarkus:dev -Dquarkus.profile=mock-llm
```

### Production Build

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

### Native Executable

```bash
./mvnw package -Dnative
./target/wspiernik-backend-1.0.0-SNAPSHOT-runner
```

## Configuration

Key properties in `application.properties`:

```properties
# Server
quarkus.http.port=8080

# Database (SQLite)
quarkus.datasource.jdbc.url=jdbc:sqlite:wspiernik.db

# LLM (Bielik)
wspiernik.llm.base-url=http://100.114.136.72:1234
wspiernik.llm.model=SpeakLeash/bielik-11b-v2.3-instruct:Q4_K_M
wspiernik.llm.max-tokens=2048
wspiernik.llm.temperature=0.7

# CORS
quarkus.http.cors=true
quarkus.http.cors.origins=/.*/
```

## API Reference

### REST Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `HEAD` | `/api/fact` | Check if facts exist (200=yes, 204=no) |
| `GET` | `/health` | Health check |
| `GET` | `/health/live` | Liveness probe |
| `GET` | `/health/ready` | Readiness probe |

### WebSocket

**Endpoint:** `ws://localhost:8080/ws`

#### Incoming Messages (Client -> Server)

```json
{
  "type": "survey_start",
  "payload": {}
}
```

| Type | Description |
|------|-------------|
| `survey_start` | Start survey session |
| `survey_message` | Send survey answer |
| `survey_complete` | End survey |
| `support_start` | Start support session |
| `support_message` | Send support message |
| `support_complete` | End support |
| `intervention_start` | Start intervention |
| `intervention_message` | Send intervention message |
| `intervention_complete` | End intervention |
| `get_facts` | Request facts list |

#### Outgoing Messages (Server -> Client)

```json
{
  "type": "survey_question",
  "payload": {
    "question": "Jak ma na imie podopieczny?"
  }
}
```

| Type | Payload | Description |
|------|---------|-------------|
| `survey_question` | `{ question }` | Next survey question |
| `survey_completed` | `{ factsSavedCount }` | Survey finished |
| `support_message` | `{ text }` | Support response |
| `support_completed` | `{ duration }` | Session ended |
| `intervention_question` | `{ question }` | Intervention question |
| `intervention_scenario_matched` | `{ scenario, severity }` | Matched scenario |
| `intervention_completed` | `{}` | Intervention finished |
| `facts_list` | `{ facts, total_count }` | List of facts |
| `facts_extracted` | `{ facts, source_message_id }` | Extracted facts |
| `error` | `{ message }` | Error message |

## Database Schema

### Facts Table

```sql
CREATE TABLE facts (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  conversation_id INTEGER,
  fact_type TEXT,        -- JSON array of tags
  fact_value TEXT,
  severity INTEGER,      -- 1-10, nullable
  extracted_at TIMESTAMP,
  created_at TIMESTAMP
);
```

## Testing

```bash
./mvnw test
```

## Health Checks

- `/health/live` - Application is running
- `/health/ready` - Application is ready to serve requests
