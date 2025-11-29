# Task 10: Implement Message Queue for Offline Resilience

## Phase
Phase 4: Polish & Cleanup

## Priority
Medium - Resilience enhancement

## Description
Implement a message queue in the WebSocket service to handle messages sent during disconnection. Messages are queued when offline and flushed when connection is restored.

## Acceptance Criteria
- [ ] Messages queued when WebSocket is disconnected
- [ ] Queue flushed automatically on reconnection
- [ ] Queue size limited to prevent memory issues
- [ ] User notified when messages are queued
- [ ] Queued messages sent in order (FIFO)

## Implementation

### Modify: `src/services/websocket.js`

Add the following to the WebSocketService class:

```javascript
class WebSocketService {
  constructor() {
    // ... existing properties ...
    this.messageQueue = [];
    this.maxQueueSize = 50;
  }

  /**
   * Send message to backend (with queue support)
   * @param {string} type - Message type
   * @param {Object} payload - Message payload
   * @param {string} requestId - Optional request ID for tracking
   * @returns {boolean} - Success status (true if sent or queued)
   */
  send(type, payload = {}, requestId = null) {
    const message = {
      type,
      payload,
      request_id: requestId || crypto.randomUUID(),
      timestamp: Date.now()
    };

    if (!this.isConnected) {
      return this.queueMessage(message);
    }

    return this.sendImmediate(message);
  }

  /**
   * Send message immediately (internal)
   * @param {Object} message - Full message object
   * @returns {boolean}
   */
  sendImmediate(message) {
    if (!this.isConnected) {
      console.error('[WS] Cannot send - not connected');
      return false;
    }

    console.log('[WS] Sending:', message.type, message);
    this.ws.send(JSON.stringify(message));
    return true;
  }

  /**
   * Queue message for later sending
   * @param {Object} message - Message to queue
   * @returns {boolean} - Whether message was queued
   */
  queueMessage(message) {
    if (this.messageQueue.length >= this.maxQueueSize) {
      console.warn('[WS] Queue full, dropping oldest message');
      this.messageQueue.shift();
    }

    this.messageQueue.push(message);
    console.log(`[WS] Message queued (${this.messageQueue.length} in queue):`, message.type);

    // Notify listeners that a message was queued
    this.notifyListeners('message_queued', {
      queueLength: this.messageQueue.length,
      message
    });

    return true;
  }

  /**
   * Flush queued messages after reconnection
   */
  flushQueue() {
    if (this.messageQueue.length === 0) return;

    console.log(`[WS] Flushing ${this.messageQueue.length} queued messages`);

    const messagesToSend = [...this.messageQueue];
    this.messageQueue = [];

    let sentCount = 0;
    let failedCount = 0;

    messagesToSend.forEach((message) => {
      // Check if message is too old (> 5 minutes)
      const age = Date.now() - message.timestamp;
      if (age > 5 * 60 * 1000) {
        console.log('[WS] Dropping stale message:', message.type);
        failedCount++;
        return;
      }

      if (this.sendImmediate(message)) {
        sentCount++;
      } else {
        // Re-queue if send failed
        this.messageQueue.push(message);
        failedCount++;
      }
    });

    // Notify about flush results
    this.notifyListeners('queue_flushed', {
      sent: sentCount,
      failed: failedCount,
      remaining: this.messageQueue.length
    });
  }

  /**
   * Get current queue status
   * @returns {Object}
   */
  get queueStatus() {
    return {
      length: this.messageQueue.length,
      maxSize: this.maxQueueSize,
      isFull: this.messageQueue.length >= this.maxQueueSize
    };
  }

  /**
   * Clear the message queue
   */
  clearQueue() {
    const clearedCount = this.messageQueue.length;
    this.messageQueue = [];
    console.log(`[WS] Queue cleared (${clearedCount} messages removed)`);
    return clearedCount;
  }

  // Modify onopen handler to flush queue
  connect(url = WS_URL) {
    return new Promise((resolve, reject) => {
      // ... existing connection setup ...

      this.ws.onopen = () => {
        console.log('[WS] Connected to', url);
        this.reconnectAttempts = 0;
        this.notifyListeners('connection', { connected: true });

        // Flush queued messages
        this.flushQueue();

        resolve();
      };

      // ... rest of connect method ...
    });
  }
}
```

### Optional: UI Indicator for Queued Messages

Add to Chat component or create separate component:

```javascript
// In Chat.jsx or new QueueIndicator.jsx
import { useEffect, useState } from 'react';
import { useWebSocketContext } from '../context/WebSocketContext';

function QueueIndicator() {
  const { subscribe } = useWebSocketContext();
  const [queueLength, setQueueLength] = useState(0);

  useEffect(() => {
    const unsubscribes = [
      subscribe('message_queued', ({ queueLength }) => {
        setQueueLength(queueLength);
      }),
      subscribe('queue_flushed', ({ remaining }) => {
        setQueueLength(remaining);
      }),
      subscribe('connection', ({ connected }) => {
        if (connected) {
          // Queue will be flushed, reset after brief delay
          setTimeout(() => setQueueLength(0), 500);
        }
      })
    ];

    return () => unsubscribes.forEach(unsub => unsub());
  }, [subscribe]);

  if (queueLength === 0) return null;

  return (
    <div className="queue-indicator" role="status" aria-live="polite">
      <span className="queue-icon">📤</span>
      <span>{queueLength} wiadomości oczekuje na wysłanie</span>
    </div>
  );
}
```

### CSS for Queue Indicator

```css
.queue-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #fef3c7;
  color: #92400e;
  border-radius: 8px;
  font-size: 12px;
  margin-bottom: 12px;
}

.queue-icon {
  font-size: 14px;
}
```

## Architecture Notes

### Why Queue Messages?

1. **Better UX**: User can keep typing during brief disconnections
2. **Data preservation**: Messages not lost during network hiccups
3. **Graceful degradation**: App remains functional during outages

### Queue Limitations

| Aspect | Value | Reason |
|--------|-------|--------|
| Max size | 50 | Prevent memory issues |
| Max age | 5 minutes | Stale messages may be irrelevant |
| Drop policy | Oldest first | Newer messages likely more relevant |

### Message Types to Queue

All message types are queued by default. Consider NOT queuing:
- `intervention_start` - Time-sensitive, may be stale
- Heartbeat/ping messages

### Alternatives Considered

**LocalStorage persistence**:
- Pro: Survives page refresh
- Con: Security concerns, complexity
- Decision: Not implemented, queue is ephemeral

**IndexedDB**:
- Pro: Large storage, structured
- Con: Overkill for this use case
- Decision: Not implemented

## Testing Strategy

```javascript
describe('Message Queue', () => {
  beforeEach(() => {
    wsService.messageQueue = [];
    wsService.ws = null;
  });

  test('queues message when disconnected', () => {
    wsService.send('test', { data: 'value' });

    expect(wsService.messageQueue).toHaveLength(1);
    expect(wsService.messageQueue[0].type).toBe('test');
  });

  test('respects max queue size', () => {
    for (let i = 0; i < 60; i++) {
      wsService.send('test', { index: i });
    }

    expect(wsService.messageQueue).toHaveLength(50);
  });

  test('flushes queue on reconnection', async () => {
    wsService.send('test1', {});
    wsService.send('test2', {});

    expect(wsService.messageQueue).toHaveLength(2);

    // Mock connection
    wsService.ws = { readyState: WebSocket.OPEN, send: jest.fn() };
    wsService.flushQueue();

    expect(wsService.messageQueue).toHaveLength(0);
    expect(wsService.ws.send).toHaveBeenCalledTimes(2);
  });

  test('drops stale messages', () => {
    const oldMessage = {
      type: 'test',
      payload: {},
      request_id: 'old',
      timestamp: Date.now() - 10 * 60 * 1000 // 10 minutes ago
    };

    wsService.messageQueue.push(oldMessage);
    wsService.ws = { readyState: WebSocket.OPEN, send: jest.fn() };
    wsService.flushQueue();

    expect(wsService.ws.send).not.toHaveBeenCalled();
  });
});
```

## Dependencies
- Task 03 (WebSocket service)

## Estimated Effort
30-40 minutes

## Files to Modify
- `src/services/websocket.js` (add queue logic)

## Optional Files to Create
- `src/components/QueueIndicator.jsx` (UI feedback)
