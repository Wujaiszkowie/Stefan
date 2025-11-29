# Task 04: Create WebSocket React Hook

## Phase
Phase 2: React Integration Layer

## Priority
High - Bridge between service and components

## Description
Create a custom React hook that provides a clean interface for components to interact with the WebSocket service. Handles subscription cleanup on unmount and provides reactive state.

## Acceptance Criteria
- [ ] Hook returns connection status state
- [ ] Hook provides `sendMessage` function
- [ ] Hook provides `subscribe` function with automatic cleanup
- [ ] Proper cleanup on component unmount
- [ ] TypeScript-friendly API (JSDoc types)

## Implementation

### File: `src/hooks/useWebSocket.js`

```javascript
/**
 * useWebSocket Hook
 *
 * React hook providing WebSocket functionality to components.
 * Handles subscription lifecycle and cleanup automatically.
 */

import { useState, useEffect, useCallback, useRef } from 'react';
import wsService from '../services/websocket';

/**
 * @typedef {Object} UseWebSocketReturn
 * @property {boolean} isConnected - Current connection status
 * @property {string} connectionState - 'connected' | 'disconnected' | 'connecting' | 'reconnecting'
 * @property {Function} sendMessage - Send message to backend
 * @property {Function} subscribe - Subscribe to message type
 */

/**
 * Hook for WebSocket communication
 * @returns {UseWebSocketReturn}
 */
export function useWebSocket() {
  const [isConnected, setIsConnected] = useState(wsService.isConnected);
  const [connectionState, setConnectionState] = useState(wsService.connectionState);

  // Track subscriptions for cleanup
  const subscriptionsRef = useRef([]);

  useEffect(() => {
    // Subscribe to connection state changes
    const unsubConnection = wsService.subscribe('connection', ({ connected }) => {
      setIsConnected(connected);
      setConnectionState(connected ? 'connected' : 'disconnected');
    });

    const unsubReconnecting = wsService.subscribe('reconnecting', () => {
      setConnectionState('reconnecting');
    });

    const unsubReconnectFailed = wsService.subscribe('reconnect_failed', () => {
      setConnectionState('disconnected');
    });

    // Sync initial state
    setIsConnected(wsService.isConnected);
    setConnectionState(wsService.connectionState);

    return () => {
      unsubConnection();
      unsubReconnecting();
      unsubReconnectFailed();

      // Cleanup all subscriptions made through this hook
      subscriptionsRef.current.forEach(unsub => unsub());
      subscriptionsRef.current = [];
    };
  }, []);

  /**
   * Send message to backend
   * @param {string} type - Message type
   * @param {Object} payload - Message payload
   * @returns {boolean} - Success status
   */
  const sendMessage = useCallback((type, payload = {}) => {
    return wsService.send(type, payload);
  }, []);

  /**
   * Subscribe to message type
   * Automatically cleans up on unmount
   * @param {string} type - Message type
   * @param {Function} callback - Callback function
   * @returns {Function} - Unsubscribe function
   */
  const subscribe = useCallback((type, callback) => {
    const unsubscribe = wsService.subscribe(type, callback);
    subscriptionsRef.current.push(unsubscribe);

    return () => {
      unsubscribe();
      const index = subscriptionsRef.current.indexOf(unsubscribe);
      if (index > -1) {
        subscriptionsRef.current.splice(index, 1);
      }
    };
  }, []);

  return {
    isConnected,
    connectionState,
    sendMessage,
    subscribe
  };
}

export default useWebSocket;
```

## Usage Example

```javascript
import { useWebSocket } from '../hooks/useWebSocket';

function MyComponent() {
  const { isConnected, sendMessage, subscribe } = useWebSocket();
  const [messages, setMessages] = useState([]);

  useEffect(() => {
    // Subscribe to message type - cleanup handled automatically
    const unsubscribe = subscribe('support_message', (msg) => {
      setMessages(prev => [...prev, msg.payload.message]);
    });

    return unsubscribe;
  }, [subscribe]);

  const handleSend = () => {
    sendMessage('support_message', { message: 'Hello' });
  };

  return (
    <div>
      <p>Status: {isConnected ? 'Connected' : 'Disconnected'}</p>
      <button onClick={handleSend} disabled={!isConnected}>
        Send
      </button>
    </div>
  );
}
```

## Architecture Notes

### Why useRef for Subscriptions?

Using `useRef` to track subscriptions allows cleanup without causing re-renders. The ref persists across renders but doesn't trigger updates when modified.

### useCallback for Stability

`sendMessage` and `subscribe` are wrapped in `useCallback` with empty dependencies because they delegate to the singleton service. This provides stable references for consumer components, preventing unnecessary re-renders and effect re-runs.

### Automatic Cleanup Pattern

The hook tracks all subscriptions made through it and cleans them up on unmount. This prevents:
1. Memory leaks from orphaned callbacks
2. State updates on unmounted components
3. Developer forgetting to unsubscribe

### Relationship to Context

This hook accesses the singleton service directly. The Context (Task 05) will use this hook internally and add:
- Conversation type state
- Connection initialization
- Shared state across components

Components can use either:
- `useWebSocket()` - Direct hook for simple cases
- `useWebSocketContext()` - Context hook for full state access

## Testing Strategy

```javascript
import { renderHook, act } from '@testing-library/react';
import { useWebSocket } from './useWebSocket';
import wsService from '../services/websocket';

jest.mock('../services/websocket');

describe('useWebSocket', () => {
  beforeEach(() => {
    wsService.isConnected = true;
    wsService.connectionState = 'connected';
    wsService.subscribe = jest.fn(() => jest.fn());
    wsService.send = jest.fn(() => true);
  });

  test('returns connection status', () => {
    const { result } = renderHook(() => useWebSocket());
    expect(result.current.isConnected).toBe(true);
  });

  test('sendMessage calls service', () => {
    const { result } = renderHook(() => useWebSocket());

    act(() => {
      result.current.sendMessage('test', { foo: 'bar' });
    });

    expect(wsService.send).toHaveBeenCalledWith('test', { foo: 'bar' });
  });

  test('cleans up subscriptions on unmount', () => {
    const unsubscribe = jest.fn();
    wsService.subscribe.mockReturnValue(unsubscribe);

    const { result, unmount } = renderHook(() => useWebSocket());

    act(() => {
      result.current.subscribe('test', () => {});
    });

    unmount();

    expect(unsubscribe).toHaveBeenCalled();
  });
});
```

## Dependencies
- Task 03 (WebSocket service)

## Estimated Effort
20-30 minutes

## Files to Create
- `src/hooks/useWebSocket.js`
