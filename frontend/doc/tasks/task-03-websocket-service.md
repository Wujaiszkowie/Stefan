# Task 03: Create WebSocket Service

## Phase
Phase 1: Infrastructure Setup

## Priority
High - Core infrastructure component

## Description
Create a singleton WebSocket service that manages the connection lifecycle, message routing, and reconnection logic. This is framework-agnostic code that can be tested independently of React.

## Acceptance Criteria
- [ ] Singleton pattern implemented
- [ ] Connection lifecycle methods: `connect()`, `disconnect()`
- [ ] Auto-reconnect with exponential backoff
- [ ] Message sending with request ID generation
- [ ] Event subscription system (pub/sub pattern)
- [ ] Connection state tracking (`isConnected` getter)
- [ ] Environment variables used for configuration

## Implementation

### File: `src/services/websocket.js`

```javascript
/**
 * WebSocket Service
 *
 * Singleton service managing WebSocket connection to backend.
 * Framework-agnostic - no React dependencies.
 */

const WS_URL = process.env.REACT_APP_WS_URL || 'ws://localhost:8080/ws';
const RECONNECT_DELAY = parseInt(process.env.REACT_APP_WS_RECONNECT_DELAY) || 3000;
const MAX_RECONNECT_ATTEMPTS = parseInt(process.env.REACT_APP_WS_MAX_RECONNECT_ATTEMPTS) || 5;

class WebSocketService {
  constructor() {
    this.ws = null;
    this.listeners = new Map();
    this.reconnectAttempts = 0;
    this.intentionalClose = false;
  }

  /**
   * Establish WebSocket connection
   * @param {string} url - WebSocket URL (optional, uses env default)
   * @returns {Promise<void>}
   */
  connect(url = WS_URL) {
    return new Promise((resolve, reject) => {
      if (this.ws?.readyState === WebSocket.OPEN) {
        resolve();
        return;
      }

      this.intentionalClose = false;
      this.ws = new WebSocket(url);

      this.ws.onopen = () => {
        console.log('[WS] Connected to', url);
        this.reconnectAttempts = 0;
        this.notifyListeners('connection', { connected: true });
        resolve();
      };

      this.ws.onclose = (event) => {
        console.log('[WS] Connection closed:', event.code, event.reason);
        this.notifyListeners('connection', { connected: false });

        if (!this.intentionalClose) {
          this.handleReconnect();
        }
      };

      this.ws.onerror = (error) => {
        console.error('[WS] Error:', error);
        this.notifyListeners('error', { error });
        reject(error);
      };

      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data);
          this.handleMessage(message);
        } catch (e) {
          console.error('[WS] Failed to parse message:', e);
        }
      };
    });
  }

  /**
   * Handle incoming message and route to subscribers
   * @param {Object} message - Parsed message object
   */
  handleMessage(message) {
    const { type } = message;
    console.log('[WS] Received:', type, message);

    // Notify type-specific listeners
    this.notifyListeners(type, message);

    // Notify 'all' listeners (for debugging/logging)
    this.notifyListeners('all', message);
  }

  /**
   * Send message to backend
   * @param {string} type - Message type
   * @param {Object} payload - Message payload
   * @param {string} requestId - Optional request ID for tracking
   * @returns {boolean} - Success status
   */
  send(type, payload = {}, requestId = null) {
    if (!this.isConnected) {
      console.error('[WS] Cannot send - not connected');
      return false;
    }

    const message = {
      type,
      payload,
      request_id: requestId || crypto.randomUUID()
    };

    console.log('[WS] Sending:', type, message);
    this.ws.send(JSON.stringify(message));
    return true;
  }

  /**
   * Subscribe to message type
   * @param {string} type - Message type to subscribe to
   * @param {Function} callback - Callback function
   * @returns {Function} - Unsubscribe function
   */
  subscribe(type, callback) {
    if (!this.listeners.has(type)) {
      this.listeners.set(type, new Set());
    }

    this.listeners.get(type).add(callback);

    // Return unsubscribe function
    return () => {
      const callbacks = this.listeners.get(type);
      if (callbacks) {
        callbacks.delete(callback);
      }
    };
  }

  /**
   * Notify all listeners for a type
   * @param {string} type - Message type
   * @param {Object} data - Data to pass to callbacks
   */
  notifyListeners(type, data) {
    const callbacks = this.listeners.get(type);
    if (callbacks) {
      callbacks.forEach(cb => {
        try {
          cb(data);
        } catch (e) {
          console.error('[WS] Listener error:', e);
        }
      });
    }
  }

  /**
   * Handle reconnection with exponential backoff
   */
  handleReconnect() {
    if (this.reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
      console.error('[WS] Max reconnect attempts reached');
      this.notifyListeners('reconnect_failed', {
        attempts: this.reconnectAttempts
      });
      return;
    }

    this.reconnectAttempts++;
    const delay = RECONNECT_DELAY * Math.pow(2, this.reconnectAttempts - 1);

    console.log(`[WS] Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})`);

    this.notifyListeners('reconnecting', {
      attempt: this.reconnectAttempts,
      maxAttempts: MAX_RECONNECT_ATTEMPTS,
      delay
    });

    setTimeout(() => {
      this.connect().catch(() => {
        // Error handled in connect()
      });
    }, delay);
  }

  /**
   * Intentionally close connection
   */
  disconnect() {
    this.intentionalClose = true;
    if (this.ws) {
      this.ws.close(1000, 'Client disconnect');
      this.ws = null;
    }
  }

  /**
   * Check if connected
   * @returns {boolean}
   */
  get isConnected() {
    return this.ws?.readyState === WebSocket.OPEN;
  }

  /**
   * Get current connection state
   * @returns {string} - 'connected' | 'disconnected' | 'connecting' | 'reconnecting'
   */
  get connectionState() {
    if (!this.ws) return 'disconnected';

    switch (this.ws.readyState) {
      case WebSocket.CONNECTING:
        return this.reconnectAttempts > 0 ? 'reconnecting' : 'connecting';
      case WebSocket.OPEN:
        return 'connected';
      case WebSocket.CLOSING:
      case WebSocket.CLOSED:
      default:
        return 'disconnected';
    }
  }
}

// Singleton export
export const wsService = new WebSocketService();
export default wsService;
```

## Architecture Notes

### Singleton Pattern

Single instance ensures:
1. One WebSocket connection per app
2. Shared state across all subscribers
3. Centralized reconnection logic

### Pub/Sub Pattern

Using `Map<string, Set<Function>>` for listeners:
- O(1) lookup by message type
- Set prevents duplicate subscriptions
- Memory-efficient cleanup via returned unsubscribe function

### Exponential Backoff

Reconnect delays: 3s → 6s → 12s → 24s → 48s

This prevents:
- Server overload during outages
- Aggressive reconnection loops
- Battery drain on mobile

### Why Framework-Agnostic?

1. **Testability**: Can test without React test utilities
2. **Reusability**: Could be used in React Native, Vue, etc.
3. **Separation of concerns**: Infrastructure vs UI logic

## Testing Strategy

```javascript
// Example unit test structure
describe('WebSocketService', () => {
  let mockWebSocket;

  beforeEach(() => {
    mockWebSocket = {
      send: jest.fn(),
      close: jest.fn(),
      readyState: WebSocket.OPEN
    };
    global.WebSocket = jest.fn(() => mockWebSocket);
  });

  test('sends message with request ID', () => {
    wsService.ws = mockWebSocket;
    wsService.send('support_start', {});

    expect(mockWebSocket.send).toHaveBeenCalledWith(
      expect.stringContaining('support_start')
    );
  });

  test('notifies subscribers on message', () => {
    const callback = jest.fn();
    wsService.subscribe('support_message', callback);

    wsService.handleMessage({ type: 'support_message', payload: { message: 'test' } });

    expect(callback).toHaveBeenCalled();
  });
});
```

## Dependencies
- Task 01 (directory structure)
- Task 02 (environment configuration)

## Estimated Effort
30-45 minutes

## Files to Create
- `src/services/websocket.js`
