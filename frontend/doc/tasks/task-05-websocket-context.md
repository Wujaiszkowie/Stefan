# Task 05: Create WebSocket Context Provider

## Phase
Phase 2: React Integration Layer

## Priority
High - Global state management for WebSocket

## Description
Create a React Context that provides centralized WebSocket state management across the application. This includes connection initialization, conversation type tracking, and shared state access.

## Acceptance Criteria
- [ ] Context provider initializes WebSocket connection on mount
- [ ] Tracks current conversation type (support/survey/intervention/null)
- [ ] Provides methods to start/end conversation flows
- [ ] Exposes connection state to all children
- [ ] Custom hook `useWebSocketContext` for easy access
- [ ] Handles cleanup on provider unmount

## Implementation

### File: `src/context/WebSocketContext.jsx`

```javascript
/**
 * WebSocket Context Provider
 *
 * Provides centralized WebSocket state management.
 * Initializes connection and tracks conversation state.
 */

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import wsService from '../services/websocket';
import { useWebSocket } from '../hooks/useWebSocket';

/**
 * @typedef {'support' | 'survey' | 'intervention' | null} ConversationType
 */

/**
 * @typedef {Object} WebSocketContextValue
 * @property {boolean} isConnected
 * @property {string} connectionState
 * @property {ConversationType} conversationType
 * @property {Function} sendMessage
 * @property {Function} subscribe
 * @property {Function} startConversation
 * @property {Function} endConversation
 */

const WebSocketContext = createContext(null);

/**
 * WebSocket Provider Component
 */
export function WebSocketProvider({ children }) {
  const { isConnected, connectionState, sendMessage, subscribe } = useWebSocket();
  const [conversationType, setConversationType] = useState(null);
  const [isInitialized, setIsInitialized] = useState(false);

  // Initialize WebSocket connection on mount
  useEffect(() => {
    wsService.connect()
      .then(() => {
        setIsInitialized(true);
      })
      .catch((error) => {
        console.error('[WebSocketContext] Connection failed:', error);
        setIsInitialized(true); // Still mark as initialized to unblock UI
      });

    return () => {
      wsService.disconnect();
    };
  }, []);

  // Listen for conversation completion events
  useEffect(() => {
    const unsubscribes = [
      subscribe('survey_completed', () => {
        console.log('[WebSocketContext] Survey completed');
        setConversationType(null);
      }),
      subscribe('intervention_completed', () => {
        console.log('[WebSocketContext] Intervention completed');
        setConversationType(null);
      }),
      subscribe('support_completed', () => {
        console.log('[WebSocketContext] Support session completed');
        setConversationType(null);
      })
    ];

    return () => unsubscribes.forEach(unsub => unsub());
  }, [subscribe]);

  /**
   * Start a conversation flow
   * @param {ConversationType} type - Conversation type to start
   * @param {Object} payload - Optional initial payload
   */
  const startConversation = useCallback((type, payload = {}) => {
    if (!isConnected) {
      console.warn('[WebSocketContext] Cannot start conversation - not connected');
      return false;
    }

    // End any existing conversation
    if (conversationType) {
      sendMessage(`${conversationType}_complete`, {});
    }

    // Start new conversation
    setConversationType(type);
    sendMessage(`${type}_start`, payload);
    return true;
  }, [isConnected, conversationType, sendMessage]);

  /**
   * End the current conversation
   */
  const endConversation = useCallback(() => {
    if (conversationType) {
      sendMessage(`${conversationType}_complete`, {});
      setConversationType(null);
    }
  }, [conversationType, sendMessage]);

  /**
   * Send message in current conversation context
   * @param {string} message - Message content
   */
  const sendChatMessage = useCallback((message) => {
    if (!conversationType) {
      console.warn('[WebSocketContext] No active conversation');
      return false;
    }

    return sendMessage(`${conversationType}_message`, { message });
  }, [conversationType, sendMessage]);

  const value = {
    // Connection state
    isConnected,
    connectionState,
    isInitialized,

    // Conversation state
    conversationType,

    // Methods
    sendMessage,
    sendChatMessage,
    subscribe,
    startConversation,
    endConversation
  };

  return (
    <WebSocketContext.Provider value={value}>
      {children}
    </WebSocketContext.Provider>
  );
}

/**
 * Hook to access WebSocket context
 * @returns {WebSocketContextValue}
 * @throws {Error} If used outside of WebSocketProvider
 */
export function useWebSocketContext() {
  const context = useContext(WebSocketContext);

  if (!context) {
    throw new Error('useWebSocketContext must be used within a WebSocketProvider');
  }

  return context;
}

export default WebSocketContext;
```

## Usage Example

### In App.jsx (Provider)
```javascript
import { WebSocketProvider } from './context/WebSocketContext';

function App() {
  return (
    <WebSocketProvider>
      <YourAppComponents />
    </WebSocketProvider>
  );
}
```

### In Components (Consumer)
```javascript
import { useWebSocketContext } from '../context/WebSocketContext';

function Chat() {
  const {
    isConnected,
    conversationType,
    sendChatMessage,
    subscribe
  } = useWebSocketContext();

  const handleSend = (message) => {
    if (conversationType) {
      sendChatMessage(message);
    }
  };

  // ...
}
```

### In ActionButtons
```javascript
import { useWebSocketContext } from '../context/WebSocketContext';

function ActionButtons() {
  const { isConnected, startConversation } = useWebSocketContext();

  return (
    <button
      onClick={() => startConversation('intervention')}
      disabled={!isConnected}
    >
      Pomoc w nagłych wypadkach
    </button>
  );
}
```

## Architecture Notes

### Context vs Direct Hook

| Aspect | `useWebSocket` | `useWebSocketContext` |
|--------|---------------|----------------------|
| Connection init | No | Yes (auto) |
| Conversation tracking | No | Yes |
| Use case | Low-level access | High-level features |
| Provider required | No | Yes |

### Why Track Conversation Type?

1. **Message routing**: Determines which `*_message` type to send
2. **UI state**: Shows which mode user is in
3. **Flow control**: Prevents starting multiple conversations
4. **Completion handling**: Auto-resets on `*_completed` events

### Initialization Pattern

Connection is established in the provider's `useEffect`:
- Single connection point for the entire app
- Cleanup on provider unmount
- `isInitialized` flag prevents flicker during connection

### Conversation Lifecycle

```
startConversation('support')
    │
    ├── If existing conversation: send *_complete
    │
    ├── Set conversationType = 'support'
    │
    └── Send 'support_start'

    ... user interaction ...

Backend sends 'support_completed'
    │
    └── Set conversationType = null
```

## Testing Strategy

```javascript
import { render, screen } from '@testing-library/react';
import { WebSocketProvider, useWebSocketContext } from './WebSocketContext';
import wsService from '../services/websocket';

jest.mock('../services/websocket');

const TestConsumer = () => {
  const { isConnected, conversationType } = useWebSocketContext();
  return (
    <div>
      <span data-testid="connected">{isConnected ? 'yes' : 'no'}</span>
      <span data-testid="type">{conversationType || 'none'}</span>
    </div>
  );
};

describe('WebSocketContext', () => {
  beforeEach(() => {
    wsService.connect.mockResolvedValue();
    wsService.isConnected = true;
    wsService.subscribe = jest.fn(() => jest.fn());
  });

  test('provides connection status', async () => {
    render(
      <WebSocketProvider>
        <TestConsumer />
      </WebSocketProvider>
    );

    expect(await screen.findByTestId('connected')).toHaveTextContent('yes');
  });

  test('throws when used outside provider', () => {
    const consoleError = jest.spyOn(console, 'error').mockImplementation();

    expect(() => {
      render(<TestConsumer />);
    }).toThrow('useWebSocketContext must be used within a WebSocketProvider');

    consoleError.mockRestore();
  });
});
```

## Dependencies
- Task 03 (WebSocket service)
- Task 04 (useWebSocket hook)

## Estimated Effort
30-40 minutes

## Files to Create
- `src/context/WebSocketContext.jsx`
