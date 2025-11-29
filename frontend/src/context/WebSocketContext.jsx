/**
 * WebSocket Context Provider
 *
 * Provides centralized WebSocket state management.
 * Initializes connection and tracks conversation state.
 * Handles conversation completion and transitions.
 */

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import wsService from '../services/websocket';
import { useWebSocket } from '../hooks/useWebSocket';

const WebSocketContext = createContext(null);

/**
 * WebSocket Provider Component
 */
export function WebSocketProvider({ children }) {
  const { isConnected, connectionState, sendMessage, subscribe } = useWebSocket();
  const [conversationType, setConversationType] = useState(null);
  const [isInitialized, setIsInitialized] = useState(false);
  const [lastCompletion, setLastCompletion] = useState(null);

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
    const handleCompletion = (type) => (msg) => {
      console.log(`[WebSocketContext] ${type} completed`, msg.payload);
      setLastCompletion({
        type,
        payload: msg.payload || {},
        timestamp: Date.now()
      });
      // Only clear conversation if it matches the completed type
      // This prevents clearing when switching between conversation types
      setConversationType(current => current === type ? null : current);
    };

    const unsubscribes = [
      subscribe('survey_completed', handleCompletion('survey')),
      subscribe('intervention_completed', handleCompletion('intervention')),
      subscribe('support_completed', handleCompletion('support'))
    ];

    return () => unsubscribes.forEach(unsub => unsub());
  }, [subscribe]);

  /**
   * Start a conversation flow
   * @param {string} type - Conversation type (support, survey, intervention)
   * @param {Object} payload - Optional initial payload
   * @returns {boolean} - Success status
   */
  const startConversation = useCallback((type, payload = {}) => {
    if (!isConnected) {
      console.warn('[WebSocketContext] Cannot start conversation - not connected');
      return false;
    }

    // Clear last completion when starting new conversation
    setLastCompletion(null);

    // End any existing conversation first
    if (conversationType && conversationType !== type) {
      console.log('[WebSocketContext] Ending existing conversation:', conversationType);
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
   * @param {string} text - Message content
   * @returns {boolean} - Success status
   */
  const sendChatMessage = useCallback((text) => {
    if (!conversationType) {
      console.warn('[WebSocketContext] No active conversation');
      return false;
    }

    return sendMessage(`${conversationType}_message`, { text });
  }, [conversationType, sendMessage]);

  /**
   * Clear last completion state
   */
  const clearLastCompletion = useCallback(() => {
    setLastCompletion(null);
  }, []);

  const value = {
    // Connection state
    isConnected,
    connectionState,
    isInitialized,

    // Conversation state
    conversationType,
    lastCompletion,

    // Methods
    sendMessage,
    sendChatMessage,
    subscribe,
    startConversation,
    endConversation,
    clearLastCompletion
  };

  return (
    <WebSocketContext.Provider value={value}>
      {children}
    </WebSocketContext.Provider>
  );
}

/**
 * Hook to access WebSocket context
 * @returns {Object} WebSocket context value
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
