# Backend Implementation Plan for Wspiernik

**Version:** 1.0
**Date:** 29 November 2025
**Technology:** Java 21, Quarkus 3.30.1

---

## Task Analysis

### Analysis of Requirements and Approach

**Current State:**
- Basic Quarkus 3.30.1 project with Java 21
- Only a simple `GreetingResource` exists
- Empty `application.properties`
- No database, WebSocket, or LLM integration

**Target State:**
Build a complete backend for Wspiernik with:
1. **WebSocket API** for real-time communication with frontend
2. **SQLite database** with Hibernate Panache for persistence
3. **LLM integration** with local Bielnik model (OpenAI-compatible API)
4. **4 functional modules**: Survey, Intervention, Support, Facts Distiller

**Architecture Decisions:**
- Package structure: `com.wspiernik.*` (change from `org.acme`)
- Use Quarkus WebSockets Next for reactive WebSocket handling
- Use Hibernate ORM with Panache (repository pattern) for SQLite
- Interface-based LLM client for easy testing/mocking
- Event-driven architecture with CDI events for async Facts Distiller

---

## Implementation Plan

### Phase 1: Foundation & Infrastructure (Tasks 1-5)

**Task 1: Project Configuration & Dependencies**
- Update `pom.xml` with required dependencies:
  - `quarkus-websockets-next` for WebSocket
  - `quarkus-hibernate-orm-panache` for ORM
  - `quarkus-jdbc-sqlite` for SQLite driver (via xerial)
  - `quarkus-rest-client-jackson` for LLM HTTP calls
  - `quarkus-scheduler` for async processing
- Rename package from `org.acme` to `com.wspiernik`

**Task 2: Application Configuration**
- Configure `application.properties`:
  - SQLite datasource (file-based: `wspiernik.db`)
  - Hibernate settings (auto-create schema)
  - LLM endpoint configuration
  - WebSocket configuration
  - Logging configuration

**Task 3: Database Entities**
- Create JPA entities with Panache:
  - `CaregiverProfile` - caregiver and ward information
  - `Conversation` - conversation records
  - `Fact` - extracted facts from conversations
  - `CrisisScenario` - predefined crisis scenarios
  - `CaregiverSupportLog` - support session logs
  - `BackendLog` - application logs

**Task 4: Repository Layer**
- Create Panache repositories for each entity
- Implement basic CRUD operations
- Add custom query methods (e.g., find facts by conversation)

**Task 5: Database Initialization**
- Create `DataInitializer` with `@Observes StartupEvent`
- Seed 3 MVP crisis scenarios (Fall, Confusion, Chest Pain)
- Create initial database schema

---

### Phase 2: LLM Integration (Tasks 6-8)

**Task 6: LLM Client Interface & DTOs**
- Define `LlmClient` interface
- Create request/response DTOs for LLM communication
- Create `LlmMessage` record for chat messages

**Task 7: Bielnik LLM Implementation**
- Implement `BielnikClient` using Quarkus REST Client
- Handle OpenAI-compatible API format
- Implement timeout and error handling
- Make endpoint configurable

**Task 8: System Prompts Management**
- Create `PromptTemplates` class with text blocks
- Define prompts for: Survey, Intervention (per scenario), Support, Facts Distiller
- Implement context injection (profile, facts)

---

### Phase 3: WebSocket Infrastructure (Tasks 9-12)

**Task 9: WebSocket DTOs**
- Create `IncomingMessage` record (type, payload, requestId)
- Create `OutgoingMessage` record (type, payload)
- Create specific payload records for each message type

**Task 10: WebSocket Endpoint**
- Create `WspiernikSocket` with `@WebSocket`
- Implement `onOpen`, `onClose`, `onMessage`, `onError`
- Session management for active connections

**Task 11: Message Dispatcher**
- Create `MessageDispatcher` service
- Route messages by type to appropriate handlers
- Handle unknown message types gracefully

**Task 12: Session Manager**
- Create `ConversationSessionManager`
- Track active conversations per WebSocket session
- Store conversation context in memory

---

### Phase 4: Domain Services - Survey (Tasks 13-15)

**Task 13: Survey Service Core**
- Create `SurveyService` with state machine
- Define survey steps (age, conditions, medications, mobility)
- Implement step progression logic

**Task 14: Survey LLM Integration**
- Generate conversational questions via LLM
- Parse and validate user responses
- Handle follow-up questions

**Task 15: Survey Completion**
- Save `CaregiverProfile` to database
- Trigger Facts Distiller event
- Send completion notification

---

### Phase 5: Domain Services - Intervention (Tasks 16-19)

**Task 16: Scenario Matching**
- Implement keyword-based scenario matching
- Load scenarios from database
- Handle unmatched scenarios (generic flow)

**Task 17: Intervention Service Core**
- Create `InterventionService`
- Manage intervention conversation flow
- Inject profile context into prompts

**Task 18: Intervention LLM Integration**
- Generate scenario-specific questions
- Build context from profile and facts
- Process user responses

**Task 19: Intervention Completion**
- Save conversation transcript
- Generate summary
- Trigger Facts Distiller event

---

### Phase 6: Domain Services - Support (Tasks 20-22)

**Task 20: Support Service Core**
- Create `SupportService`
- Implement empathetic conversation flow
- Load context (profile + facts) on start

**Task 21: Support LLM Integration**
- Use Support system prompt
- Maintain conversation history
- Generate empathetic responses

**Task 22: Support Completion**
- Save conversation and support log
- Trigger Facts Distiller event
- Send completion notification

---

### Phase 7: Facts Distiller (Tasks 23-25)

**Task 23: Facts Distiller Service**
- Create `FactsDistillerService`
- Listen for `ConversationCompletedEvent`
- Process asynchronously (non-blocking)

**Task 24: Facts Extraction Logic**
- Send transcript + existing facts to LLM
- Parse JSON response with extracted facts
- Compare with existing facts (deduplication)

**Task 25: Facts Persistence & Notification**
- Save new facts to database
- Send `facts_extracted` WebSocket notification
- Update conversation `facts_extracted` flag

---

### Phase 8: Logging & Error Handling (Tasks 26-28)

**Task 26: Custom SQLite Logger**
- Create `SqliteLogger` service
- Log INFO/ERROR events to `backend_logs` table
- Include module, message, details

**Task 27: Global Error Handler**
- Create WebSocket error handler
- Log errors to database
- Return generic error messages to client

**Task 28: Integration & Cleanup**
- Remove old `GreetingResource`
- Add health check endpoint
- Final integration testing setup

---

## Summary

| Phase | Tasks | Description |
|-------|-------|-------------|
| 1 | 1-5 | Foundation & Infrastructure |
| 2 | 6-8 | LLM Integration |
| 3 | 9-12 | WebSocket Infrastructure |
| 4 | 13-15 | Survey Module |
| 5 | 16-19 | Intervention Module |
| 6 | 20-22 | Support Module |
| 7 | 23-25 | Facts Distiller |
| 8 | 26-28 | Logging & Polish |

**Total: 28 Tasks in 8 Phases**

---

## Directory Structure (Target)

```
src/main/java/com/wspiernik/
├── api/
│   ├── dto/           # JSON Records (IncomingMessage, OutgoingMessage)
│   └── websocket/     # Socket endpoint and Dispatcher
├── domain/
│   ├── survey/        # Survey logic
│   ├── intervention/  # Intervention logic
│   ├── support/       # Support logic
│   └── facts/         # Distiller logic
├── infrastructure/
│   ├── llm/           # Bielnik integration
│   ├── persistence/   # Hibernate Entities & Repositories
│   └── lifecycle/     # Startup scripts
└── utils/             # JSON parsers, Date formatters
```

---

## Discussion Points

1. **SQLite Driver**: Plan uses `io.quarkiverse.jdbc:quarkus-jdbc-sqlite` from Quarkiverse.

2. **LLM API Format**: Assumes Bielnik exposes an OpenAI-compatible API (POST `/v1/chat/completions`).

3. **Package Naming**: Using `com.wspiernik` as base package.

4. **WebSocket vs REST for Facts Query**: `get_facts` and `get_profile` implemented as WebSocket messages per PRD spec.

5. **Docker Compose**: Not included in this backend implementation plan (handled separately).

---

## Implementation Strategy (3x3 Workflow)

Implementation will follow the 3x3 strategy:
- Work in batches of up to 3 tasks
- Present completed work after each batch
- Wait for approval before proceeding to next batch
- Total of ~10 implementation cycles expected
