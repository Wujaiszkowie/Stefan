/**
 * WebSocket Service
 *
 * Singleton service managing WebSocket connection to backend.
 * Framework-agnostic - no React dependencies.
 * Includes message queue for offline resilience.
 */

const WS_URL = process.env.REACT_APP_WS_URL || 'ws://localhost:8080/ws';
const RECONNECT_DELAY = parseInt(process.env.REACT_APP_WS_RECONNECT_DELAY) || 3000;
const MAX_RECONNECT_ATTEMPTS = parseInt(process.env.REACT_APP_WS_MAX_RECONNECT_ATTEMPTS) || 5;
const MAX_QUEUE_SIZE = 50;
const MAX_MESSAGE_AGE_MS = 5 * 60 * 1000; // 5 minutes

class WebSocketService {
  constructor() {
    this.ws = null;
    this.listeners = new Map();
    this.reconnectAttempts = 0;
    this.intentionalClose = false;
    this.messageQueue = [];
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

        // Flush queued messages on reconnection
        this.flushQueue();

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

    // Remove timestamp before sending (internal use only)
    const { timestamp, ...messageToSend } = message;

    console.log('[WS] Sending:', message.type, messageToSend);
    this.ws.send(JSON.stringify(messageToSend));
    return true;
  }

  /**
   * Queue message for later sending
   * @param {Object} message - Message to queue
   * @returns {boolean} - Whether message was queued
   */
  queueMessage(message) {
    if (this.messageQueue.length >= MAX_QUEUE_SIZE) {
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
    let droppedCount = 0;

    messagesToSend.forEach((message) => {
      // Check if message is too old
      const age = Date.now() - message.timestamp;
      if (age > MAX_MESSAGE_AGE_MS) {
        console.log('[WS] Dropping stale message:', message.type);
        droppedCount++;
        return;
      }

      if (this.sendImmediate(message)) {
        sentCount++;
      } else {
        // Re-queue if send failed
        this.messageQueue.push(message);
      }
    });

    // Notify about flush results
    this.notifyListeners('queue_flushed', {
      sent: sentCount,
      dropped: droppedCount,
      remaining: this.messageQueue.length
    });

    console.log(`[WS] Queue flushed: ${sentCount} sent, ${droppedCount} dropped`);
  }

  /**
   * Get current queue status
   * @returns {Object}
   */
  get queueStatus() {
    return {
      length: this.messageQueue.length,
      maxSize: MAX_QUEUE_SIZE,
      isFull: this.messageQueue.length >= MAX_QUEUE_SIZE
    };
  }

  /**
   * Clear the message queue
   * @returns {number} - Number of messages cleared
   */
  clearQueue() {
    const clearedCount = this.messageQueue.length;
    this.messageQueue = [];
    console.log(`[WS] Queue cleared (${clearedCount} messages removed)`);
    return clearedCount;
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
