# Frontend-Backend Integration Guide

This document describes the integration between the React frontend and Quarkus backend via WebSocket communication.

---

## Table of Contents

1. [Overview](#overview)
2. [Connection Details](#connection-details)
3. [Message Protocol](#message-protocol)
4. [Message Types](#message-types)
5. [Implementation Plan](#implementation-plan)
6. [File Structure](#file-structure)
7. [Code Examples](#code-examples)
8. [Error Handling](#error-handling)
9. [Configuration](#configuration)

---

## Overview

The backend provides a WebSocket endpoint for real-time bidirectional communication. The frontend must establish a WebSocket connection to interact with the AI assistant, survey system, crisis intervention, and support features.

### Current State

| Component | Current | Target |
|-----------|---------|--------|
| Frontend | HTTP fetch to `/api/v1/chat` | WebSocket to `/ws` |
| Backend | WebSocket at `/ws` ready | No changes needed |
| Port | Frontend expects 8000 | Backend runs on 8080 |

---

## Connection Details

### WebSocket Endpoint

```
ws://localhost:8080/ws
```

### CORS Configuration (Backend)

The backend already allows connections from:
- `http://localhost:3000`
- `http://localhost:5173`

### Keep-Alive

The backend sends auto-ping every **30 seconds** to maintain connection.

---

## Message Protocol

All messages use JSON format with the following structure:

### Outgoing Messages (Frontend → Backend)

```json
{
  "type": "message_type",
  "payload": {
    // type-specific data
  },
  "request_id": "optional-uuid-for-tracking"
}
```

### Incoming Messages (Backend → Frontend)

```json
{
  "type": "response_type",
  "payload": {
    // type-specific data
  },
  "request_id": "echoed-from-request"
}
```

---

## Message Types

### Survey Flow

Used for collecting caregiver profile information (ward age, conditions, medications, mobility, other info).

| Direction | Type | Payload | Description |
|-----------|------|---------|-------------|
| → | `survey_start` | `{}` | Start profile survey |
| ← | `survey_question` | `{question, step}` | Question to display |
| → | `survey_message` | `{message}` | User's answer |
| → | `survey_complete` | `{}` | End survey early |
| ← | `survey_completed` | `{profileId, factsSavedCount}` | Survey finished |

**Survey Steps:** `WARD_AGE` → `CONDITIONS` → `MEDICATIONS` → `MOBILITY` → `OTHER` → `CONFIRMATION` → `COMPLETED`

### Intervention Flow (Emergency)

Used for crisis situations requiring immediate guidance.

| Direction | Type | Payload | Description |
|-----------|------|---------|-------------|
| → | `intervention_start` | `{situation?}` | Start crisis intervention |
| ← | `intervention_scenario_matched` | `{scenario, severity}` | Matched crisis scenario |
| ← | `intervention_question` | `{question, step}` | Follow-up question |
| → | `intervention_message` | `{message}` | User's response |
| → | `intervention_complete` | `{}` | End intervention |
| ← | `intervention_completed` | `{scenarioId, completed}` | Intervention finished |

**Scenario Types:** `fall`, `confusion`, `chest_pain`

### Support Flow (Mental Support)

Used for emotional support and general assistance conversations.

| Direction | Type | Payload | Description |
|-----------|------|---------|-------------|
| → | `support_start` | `{}` | Start support session |
| ← | `support_message` | `{message, timestamp}` | AI response |
| → | `support_message` | `{message}` | User's message |
| → | `support_complete` | `{}` | End support session |
| ← | `support_completed` | `{sessionId, duration}` | Session finished |

### Query Operations

Used for retrieving stored data.

| Direction | Type | Payload | Description |
|-----------|------|---------|-------------|
| → | `get_facts` | `{}` | Request extracted facts |
| ← | `facts_list` | `{facts[], totalCount}` | Facts response |
| → | `get_profile` | `{}` | Request caregiver profile |
| ← | `profile_data` | `{wardAge, conditions, medications, mobility, otherInfo}` | Profile response |

### Async Events

Events pushed by backend after async processing.

| Direction | Type | Payload | Description |
|-----------|------|---------|-------------|
| ← | `facts_extracted` | `{conversationId, factsCount, facts[]}` | Facts extraction complete |

### Error Messages

| Direction | Type | Payload | Description |
|-----------|------|---------|-------------|
| ← | `error` | `{message, code, requestId}` | Error response |

---

## Implementation Plan

### Phase 1: WebSocket Service Layer

Create a singleton WebSocket service to manage the connection.

**File:** `src/services/websocket.js`

Features:
- Connection lifecycle management
- Auto-reconnect with exponential backoff
- Message sending with request ID tracking
- Event subscription system
- Connection state tracking

### Phase 2: React Hook

Create a custom hook for React components.

**File:** `src/hooks/useWebSocket.js`

Features:
- Connection status state
- Send message function
- Subscribe to message types
- Automatic cleanup on unmount

### Phase 3: Context Provider

Create a context for global WebSocket state.

**File:** `src/context/WebSocketContext.jsx`

Features:
- Centralized connection state
- Message history
- Session management
- Share state across components

### Phase 4: Component Integration

Refactor existing components to use WebSocket.

**Files to modify:**
- `src/components/Chat.jsx` - Replace fetch with WebSocket
- `src/components/ActionButtons.jsx` - Trigger intervention/support flows
- `src/App.jsx` - Add WebSocketProvider wrapper

### Phase 5: Error Handling & Resilience

- Parse and display backend errors (Polish language)
- Implement reconnection logic
- Queue messages during disconnect
- Session recovery

---

## File Structure

```
src/
├── services/
│   └── websocket.js          # WebSocket connection manager
├── hooks/
│   └── useWebSocket.js       # React hook for WebSocket
├── context/
│   └── WebSocketContext.jsx  # Global WebSocket state
├── components/
│   ├── Chat.jsx              # Modified for WebSocket
│   ├── ActionButtons.jsx     # Modified for flows
│   └── ConnectionStatus.jsx  # New: connection indicator
└── App.jsx                   # Add provider wrapper
```

---

## Code Examples

### WebSocket Service

```javascript
// src/services/websocket.js

class WebSocketService {
  constructor() {
    this.ws = null;
    this.listeners = new Map();
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectDelay = 3000;
  }

  connect(url = 'ws://localhost:8080/ws') {
    return new Promise((resolve, reject) => {
      this.ws = new WebSocket(url);

      this.ws.onopen = () => {
        console.log('WebSocket connected');
        this.reconnectAttempts = 0;
        resolve();
      };

      this.ws.onclose = (event) => {
        console.log('WebSocket closed:', event.code);
        this.handleReconnect();
      };

      this.ws.onerror = (error) => {
        console.error('WebSocket error:', error);
        reject(error);
      };

      this.ws.onmessage = (event) => {
        this.handleMessage(JSON.parse(event.data));
      };
    });
  }

  handleMessage(message) {
    const { type } = message;
    const callbacks = this.listeners.get(type) || [];
    callbacks.forEach(cb => cb(message));

    // Also notify 'all' listeners
    const allCallbacks = this.listeners.get('all') || [];
    allCallbacks.forEach(cb => cb(message));
  }

  send(type, payload = {}, requestId = null) {
    if (this.ws?.readyState !== WebSocket.OPEN) {
      console.error('WebSocket not connected');
      return false;
    }

    const message = {
      type,
      payload,
      request_id: requestId || crypto.randomUUID()
    };

    this.ws.send(JSON.stringify(message));
    return true;
  }

  subscribe(type, callback) {
    if (!this.listeners.has(type)) {
      this.listeners.set(type, []);
    }
    this.listeners.get(type).push(callback);

    // Return unsubscribe function
    return () => {
      const callbacks = this.listeners.get(type);
      const index = callbacks.indexOf(callback);
      if (index > -1) callbacks.splice(index, 1);
    };
  }

  handleReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('Max reconnect attempts reached');
      return;
    }

    this.reconnectAttempts++;
    const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);

    console.log(`Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts})`);
    setTimeout(() => this.connect(), delay);
  }

  disconnect() {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  get isConnected() {
    return this.ws?.readyState === WebSocket.OPEN;
  }
}

export const wsService = new WebSocketService();
export default wsService;
```

### React Hook

```javascript
// src/hooks/useWebSocket.js

import { useState, useEffect, useCallback } from 'react';
import wsService from '../services/websocket';

export function useWebSocket() {
  const [isConnected, setIsConnected] = useState(false);
  const [lastMessage, setLastMessage] = useState(null);

  useEffect(() => {
    // Connect on mount
    wsService.connect()
      .then(() => setIsConnected(true))
      .catch(() => setIsConnected(false));

    // Subscribe to all messages
    const unsubscribe = wsService.subscribe('all', (message) => {
      setLastMessage(message);
    });

    return () => {
      unsubscribe();
    };
  }, []);

  const sendMessage = useCallback((type, payload) => {
    return wsService.send(type, payload);
  }, []);

  const subscribe = useCallback((type, callback) => {
    return wsService.subscribe(type, callback);
  }, []);

  return {
    isConnected,
    lastMessage,
    sendMessage,
    subscribe
  };
}
```

### Chat Component Integration

```javascript
// src/components/Chat.jsx (modified)

import React, { useState, useEffect, useRef } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';

function Chat() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [conversationType, setConversationType] = useState('support');
  const messagesEndRef = useRef(null);

  const { isConnected, sendMessage, subscribe } = useWebSocket();

  useEffect(() => {
    // Subscribe to relevant message types
    const unsubscribes = [
      subscribe('support_message', (msg) => {
        setLoading(false);
        setMessages(prev => [...prev, {
          id: Date.now(),
          role: 'assistant',
          content: msg.payload.message
        }]);
      }),
      subscribe('survey_question', (msg) => {
        setLoading(false);
        setMessages(prev => [...prev, {
          id: Date.now(),
          role: 'assistant',
          content: msg.payload.question
        }]);
      }),
      subscribe('intervention_question', (msg) => {
        setLoading(false);
        setMessages(prev => [...prev, {
          id: Date.now(),
          role: 'assistant',
          content: msg.payload.question
        }]);
      }),
      subscribe('error', (msg) => {
        setLoading(false);
        setMessages(prev => [...prev, {
          id: Date.now(),
          role: 'assistant',
          content: `Błąd: ${msg.payload.message}`
        }]);
      })
    ];

    return () => unsubscribes.forEach(unsub => unsub());
  }, [subscribe]);

  const handleSend = () => {
    if (!input.trim() || !isConnected) return;

    const userMessage = {
      id: Date.now(),
      role: 'user',
      content: input.trim()
    };

    setMessages(prev => [...prev, userMessage]);
    setInput('');
    setLoading(true);

    // Send via WebSocket based on conversation type
    const messageType = `${conversationType}_message`;
    sendMessage(messageType, { message: userMessage.content });
  };

  const startConversation = (type) => {
    setConversationType(type);
    sendMessage(`${type}_start`, {});
    setLoading(true);
  };

  // ... rest of component
}
```

### Action Buttons Integration

```javascript
// src/components/ActionButtons.jsx (modified)

import React from 'react';
import { useWebSocket } from '../hooks/useWebSocket';

function ActionButtons({ onStartConversation }) {
  const { sendMessage, isConnected } = useWebSocket();

  const handleEmergency = () => {
    sendMessage('intervention_start', {});
    onStartConversation?.('intervention');
  };

  const handleMentalSupport = () => {
    sendMessage('support_start', {});
    onStartConversation?.('support');
  };

  return (
    <div className="action-buttons">
      <button
        className="btn-large btn-emergency"
        onClick={handleEmergency}
        disabled={!isConnected}
      >
        Pomoc w nagłych wypadkach
      </button>
      <button
        className="btn-large btn-support"
        onClick={handleMentalSupport}
        disabled={!isConnected}
      >
        Wsparcie psychiczne
      </button>
    </div>
  );
}
```

---

## Error Handling

### Error Response Structure

```json
{
  "type": "error",
  "payload": {
    "message": "Opis błędu po polsku",
    "code": "ERROR_CODE",
    "requestId": "original-request-id"
  }
}
```

### Common Error Codes

| Code | Description |
|------|-------------|
| `INVALID_MESSAGE_FORMAT` | Malformed JSON or missing fields |
| `UNKNOWN_MESSAGE_TYPE` | Unrecognized message type |
| `SESSION_NOT_FOUND` | No active session for connection |
| `LLM_ERROR` | LLM service unavailable |
| `DATABASE_ERROR` | Database operation failed |

### Frontend Error Handling

```javascript
subscribe('error', (msg) => {
  const { message, code, requestId } = msg.payload;

  // Log for debugging
  console.error(`[${code}] ${message} (request: ${requestId})`);

  // Display user-friendly message
  showNotification({
    type: 'error',
    message: message // Already in Polish
  });
});
```

---

## Configuration

### Environment Variables

Create `.env` file in frontend root:

```env
REACT_APP_WS_URL=ws://localhost:8080/ws
REACT_APP_WS_RECONNECT_DELAY=3000
REACT_APP_WS_MAX_RECONNECT_ATTEMPTS=5
```

### Usage in Code

```javascript
const WS_URL = process.env.REACT_APP_WS_URL || 'ws://localhost:8080/ws';
```

### Docker Configuration

Update `docker-compose.yml` to expose WebSocket port:

```yaml
services:
  backend:
    ports:
      - "8080:8080"

  frontend:
    environment:
      - REACT_APP_WS_URL=ws://backend:8080/ws
```

---

## Backend API Reference

### REST Endpoints (Auxiliary)

These endpoints remain available for health checks and testing:

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/ping` | Liveness check (returns "pong") |
| GET | `/api/health` | Component health status |
| GET | `/api/test/llm/ping` | LLM service check |
| POST | `/api/test/llm/chat` | Test LLM chat |

### Health Check Response

```json
{
  "status": "UP",
  "timestamp": "2024-01-15T10:30:00",
  "database": {
    "name": "SQLite",
    "status": "UP",
    "message": "Connected"
  },
  "llm": {
    "name": "Bielnik",
    "status": "UP",
    "message": "Available"
  }
}
```

---

## Testing

### Manual Testing

1. Open browser DevTools → Network → WS tab
2. Connect to `ws://localhost:8080/ws`
3. Send test message:
   ```json
   {"type": "support_start", "payload": {}, "request_id": "test-1"}
   ```
4. Verify response received

### Automated Testing

Use Jest with mock WebSocket:

```javascript
// src/services/__tests__/websocket.test.js

import WS from 'jest-websocket-mock';
import wsService from '../websocket';

describe('WebSocket Service', () => {
  let server;

  beforeEach(() => {
    server = new WS('ws://localhost:8080/ws');
  });

  afterEach(() => {
    WS.clean();
  });

  test('connects to server', async () => {
    await wsService.connect('ws://localhost:8080/ws');
    await server.connected;
    expect(wsService.isConnected).toBe(true);
  });

  test('sends messages', async () => {
    await wsService.connect('ws://localhost:8080/ws');
    await server.connected;

    wsService.send('support_start', {});

    await expect(server).toReceiveMessage(
      expect.stringContaining('support_start')
    );
  });
});
```

---

## Migration Checklist

- [ ] Create `src/services/websocket.js`
- [ ] Create `src/hooks/useWebSocket.js`
- [ ] Create `src/context/WebSocketContext.jsx`
- [ ] Create `.env` with WebSocket URL
- [ ] Modify `src/App.jsx` to add WebSocketProvider
- [ ] Modify `src/components/Chat.jsx` to use WebSocket
- [ ] Modify `src/components/ActionButtons.jsx` for flows
- [ ] Add connection status indicator component
- [ ] Update Docker configuration
- [ ] Test all message flows
- [ ] Remove old fetch-based API code