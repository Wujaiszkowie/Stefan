/**
 * useWebSocket Hook
 *
 * React hook providing WebSocket functionality to components.
 * Handles subscription lifecycle and cleanup automatically.
 */

import { useState, useEffect, useCallback, useRef } from 'react';
import wsService from '../services/websocket';

/**
 * Hook for WebSocket communication
 * @returns {Object} WebSocket state and methods
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
