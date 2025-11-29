/**
 * FABs Component (M3)
 *
 * Material Design 3 Floating Action Buttons for primary actions.
 * - Emergency FAB (Extended, red) - Always visible, prominent
 * - Support FAB (Regular, blue) - Secondary action
 */

import React from 'react';
import { useWebSocketContext } from '../context/WebSocketContext';

/**
 * @param {Object} props
 * @param {Function} [props.scrollToChat] - Function to scroll to chat section
 */
const FABs = ({ scrollToChat }) => {
  const { isConnected, conversationType, startConversation } = useWebSocketContext();

  const handleAction = (type) => {
    if (!isConnected) return;

    const success = startConversation(type);

    if (success && scrollToChat) {
      setTimeout(() => {
        scrollToChat();
      }, 100);
    }
  };

  const isEmergencyActive = conversationType === 'intervention';
  const isSupportActive = conversationType === 'support';

  return (
    <div className="fab-container">
      {/* Emergency FAB - Extended, always prominent */}
      <button
        className={`fab fab-extended fab-emergency ${isEmergencyActive ? 'fab-active' : ''}`}
        onClick={() => handleAction('intervention')}
        disabled={!isConnected}
        aria-label="Pomoc w nagBych wypadkach"
        aria-pressed={isEmergencyActive}
        title={!isConnected ? 'Brak poBczenia' : 'Pomoc w nagBych wypadkach'}
      >
        <svg
          width="24"
          height="24"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
          <line x1="12" y1="9" x2="12" y2="13" />
          <line x1="12" y1="17" x2="12.01" y2="17" />
        </svg>
        <span className="fab-label">Pomoc!</span>
      </button>

      {/* Support FAB - Regular size */}
      <button
        className={`fab fab-regular fab-support ${isSupportActive ? 'fab-active' : ''}`}
        onClick={() => handleAction('support')}
        disabled={!isConnected}
        aria-label="Wsparcie psychiczne"
        aria-pressed={isSupportActive}
        title={!isConnected ? 'Brak poBczenia' : 'Wsparcie psychiczne'}
      >
        <svg
          width="24"
          height="24"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
        </svg>
      </button>

      {/* Connection warning tooltip */}
      {!isConnected && (
        <div className="fab-tooltip" role="alert">
          Brak poBczenia
        </div>
      )}
    </div>
  );
};

export default FABs;
