# WebSocket Integration Plan

## Overview

This document outlines the plan to migrate the frontend from HTTP fetch-based communication to WebSocket-based real-time bidirectional communication with the Quarkus backend.

## Current State

| Aspect | Current | Target |
|--------|---------|--------|
| Protocol | HTTP fetch | WebSocket |
| Endpoint | `http://localhost:8000/api/v1/chat` | `ws://localhost:8080/ws` |
| Communication | Request/Response | Bidirectional real-time |
| Conversation Types | Single (chat) | Three flows (support, survey, intervention) |
| State Management | Local component state | Context-based global state |

## Architecture Decisions

### Why WebSocket over HTTP?

1. **Real-time bidirectional communication** - Backend can push messages without polling
2. **Session management** - Single persistent connection per user session
3. **Multiple conversation flows** - Survey, intervention, support require stateful conversations
4. **Backend auto-ping** - 30-second keep-alive already implemented

### State Management Choice: Context API

Given the scope (single WebSocket connection, shared across few components), React Context is the right choice over Redux/Zustand:

- **Pros**: No additional dependencies, sufficient for this use case, built-in React
- **Trade-off**: Not ideal for high-frequency updates, but WebSocket messages are infrequent enough

### Service Layer Pattern

Singleton WebSocket service separate from React components:

```
┌─────────────────────────────────────────────────────────┐
│                     React Components                     │
│  (Chat, ActionButtons, ConnectionStatus)                │
└─────────────────────┬───────────────────────────────────┘
                      │ useWebSocket hook
┌─────────────────────▼───────────────────────────────────┐
│                 WebSocketContext                         │
│  (connection state, conversation type, subscriptions)   │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│                 WebSocketService                         │
│  (singleton, connection lifecycle, message routing)     │
└─────────────────────────────────────────────────────────┘
```

## File Structure

```
src/
├── services/
│   └── websocket.js          # WebSocket connection manager (singleton)
├── hooks/
│   └── useWebSocket.js       # React hook for WebSocket access
├── context/
│   └── WebSocketContext.jsx  # Global WebSocket state provider
├── components/
│   ├── Chat.jsx              # Refactored for WebSocket
│   ├── ActionButtons.jsx     # Three action buttons with flows
│   └── ConnectionStatus.jsx  # Connection indicator (new)
└── App.jsx                   # Wrapped with WebSocketProvider
```

## Implementation Phases

### Phase 1: Infrastructure (Tasks 1-3)
Foundation layer - directory structure, configuration, WebSocket service.

### Phase 2: React Integration (Tasks 4-6)
Hook, context, and status component - bridging service to React.

### Phase 3: Component Integration (Tasks 7-9)
Refactor existing components to use WebSocket.

### Phase 4: Polish & Cleanup (Tasks 10-12)
Resilience features, completion handling, final cleanup.

## Message Flow Diagrams

### Support Flow
```
User clicks "Wsparcie psychiczne"
    │
    ▼
Frontend sends: { type: "support_start", payload: {} }
    │
    ▼
Backend sends: { type: "support_message", payload: { message: "..." } }
    │
    ▼
User types message
    │
    ▼
Frontend sends: { type: "support_message", payload: { message: "..." } }
    │
    ▼
Backend sends: { type: "support_message", payload: { message: "..." } }
    │
    ▼
[Loop continues until session ends]
    │
    ▼
Backend sends: { type: "support_completed", payload: { sessionId, duration } }
```

### Intervention Flow (Emergency)
```
User clicks "Pomoc w nagłych wypadkach"
    │
    ▼
Frontend sends: { type: "intervention_start", payload: {} }
    │
    ▼
Backend sends: { type: "intervention_scenario_matched", payload: { scenario, severity } }
    │
    ▼
Backend sends: { type: "intervention_question", payload: { question, step } }
    │
    ▼
User responds
    │
    ▼
Frontend sends: { type: "intervention_message", payload: { message: "..." } }
    │
    ▼
[Loop until intervention complete]
    │
    ▼
Backend sends: { type: "intervention_completed", payload: { scenarioId, completed } }
```

### Survey Flow
```
User clicks "Uzupełnij profil"
    │
    ▼
Frontend sends: { type: "survey_start", payload: {} }
    │
    ▼
Backend sends: { type: "survey_question", payload: { question, step: "WARD_AGE" } }
    │
    ▼
User answers
    │
    ▼
Frontend sends: { type: "survey_message", payload: { message: "..." } }
    │
    ▼
[Steps: WARD_AGE → CONDITIONS → MEDICATIONS → MOBILITY → OTHER → CONFIRMATION]
    │
    ▼
Backend sends: { type: "survey_completed", payload: { profileId, factsSavedCount } }
```

## Risk Mitigation

| Risk | Mitigation |
|------|------------|
| Connection drops | Auto-reconnect with exponential backoff |
| Message loss during disconnect | Message queue with flush on reconnect |
| User confusion on disconnect | Visual connection status indicator |
| Race conditions | Request ID tracking for message correlation |

## Testing Strategy

1. **Unit tests**: WebSocket service methods (mock WebSocket)
2. **Integration tests**: Hook and context behavior
3. **E2E tests**: Full flows with Playwright/Cypress
4. **Manual testing**: All three conversation flows

## Dependencies

No new npm dependencies required - using native WebSocket API.

## Task Files

Individual implementation tasks are in `frontend/doc/tasks/`:

- `task-01-directory-structure.md`
- `task-02-env-configuration.md`
- `task-03-websocket-service.md`
- `task-04-websocket-hook.md`
- `task-05-websocket-context.md`
- `task-06-connection-status.md`
- `task-07-app-wrapper.md`
- `task-08-chat-refactor.md`
- `task-09-action-buttons.md`
- `task-10-message-queue.md`
- `task-11-conversation-completion.md`
- `task-12-cleanup-testing.md`
