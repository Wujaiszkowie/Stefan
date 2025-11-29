/**
 * Connection Status Indicator
 *
 * Visual indicator for WebSocket connection state.
 * Designed for placement in header/navigation.
 */

import React from 'react';
import { useWebSocketContext } from '../context/WebSocketContext';

const STATUS_CONFIG = {
  connected: {
    color: '#22c55e',
    bgColor: '#dcfce7',
    label: 'Połączono',
    ariaLabel: 'Połączenie aktywne'
  },
  disconnected: {
    color: '#ef4444',
    bgColor: '#fee2e2',
    label: 'Rozłączono',
    ariaLabel: 'Brak połączenia'
  },
  connecting: {
    color: '#eab308',
    bgColor: '#fef9c3',
    label: 'Łączenie...',
    ariaLabel: 'Nawiązywanie połączenia'
  },
  reconnecting: {
    color: '#f97316',
    bgColor: '#ffedd5',
    label: 'Ponowne łączenie...',
    ariaLabel: 'Ponowne nawiązywanie połączenia'
  }
};

/**
 * @param {Object} props
 * @param {boolean} [props.showLabel=true] - Whether to show text label
 * @param {string} [props.className] - Additional CSS classes
 */
function ConnectionStatus({ showLabel = true, className = '' }) {
  const { connectionState, isInitialized } = useWebSocketContext();

  // Don't render until initialized to prevent flicker
  if (!isInitialized) {
    return null;
  }

  const config = STATUS_CONFIG[connectionState] || STATUS_CONFIG.disconnected;

  return (
    <div
      className={`connection-status ${className}`}
      role="status"
      aria-label={config.ariaLabel}
      aria-live="polite"
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: '6px',
        padding: '4px 10px',
        borderRadius: '9999px',
        backgroundColor: config.bgColor,
        fontSize: '12px',
        fontWeight: '500'
      }}
    >
      <span
        aria-hidden="true"
        style={{
          width: '8px',
          height: '8px',
          borderRadius: '50%',
          backgroundColor: config.color,
          animation: connectionState === 'reconnecting' ? 'pulse 1.5s infinite' : 'none'
        }}
      />
      {showLabel && (
        <span style={{ color: config.color }}>{config.label}</span>
      )}
    </div>
  );
}

export default ConnectionStatus;
