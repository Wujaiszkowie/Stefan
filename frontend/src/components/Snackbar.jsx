/**
 * Snackbar Component (M3)
 *
 * Material Design 3 Snackbar for brief messages.
 * Used for connection status notifications.
 */

import React, { useEffect, useState } from 'react';
import { useWebSocketContext } from '../context/WebSocketContext';

/**
 * Connection Snackbar - shows when disconnected
 */
const ConnectionSnackbar = () => {
  const { isConnected, connectionState } = useWebSocketContext();
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    // Show snackbar when disconnected
    if (!isConnected && connectionState !== 'connecting') {
      setVisible(true);
    } else {
      setVisible(false);
    }
  }, [isConnected, connectionState]);

  if (!visible) return null;

  const handleRetry = () => {
    // Trigger reconnect by refreshing the page
    // In a real app, you'd call a reconnect method
    window.location.reload();
  };

  return (
    <div className="snackbar snackbar-error" role="alert">
      <span>Brak połączenia z serwerem</span>
      <button className="snackbar-action" onClick={handleRetry}>
        Ponów
      </button>
    </div>
  );
};

/**
 * Generic Snackbar Component
 * @param {Object} props
 * @param {string} props.message - Message to display
 * @param {string} props.variant - 'default' | 'error'
 * @param {string} props.actionLabel - Optional action button label
 * @param {Function} props.onAction - Optional action callback
 * @param {Function} props.onDismiss - Optional dismiss callback
 * @param {number} props.duration - Auto-dismiss duration in ms (0 = no auto-dismiss)
 */
const Snackbar = ({
  message,
  variant = 'default',
  actionLabel,
  onAction,
  onDismiss,
  duration = 4000
}) => {
  useEffect(() => {
    if (duration > 0 && onDismiss) {
      const timer = setTimeout(onDismiss, duration);
      return () => clearTimeout(timer);
    }
  }, [duration, onDismiss]);

  return (
    <div
      className={`snackbar ${variant === 'error' ? 'snackbar-error' : ''}`}
      role="alert"
    >
      <span>{message}</span>
      {actionLabel && onAction && (
        <button className="snackbar-action" onClick={onAction}>
          {actionLabel}
        </button>
      )}
    </div>
  );
};

export { ConnectionSnackbar, Snackbar };
export default Snackbar;
