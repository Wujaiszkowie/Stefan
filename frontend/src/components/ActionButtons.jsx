/**
 * Action Buttons Component
 *
 * Primary action buttons for starting conversation flows.
 * - Emergency (Intervention) - for crisis situations
 * - Mental Support - for emotional support
 * - Survey - for profile completion
 */

import React from 'react';
import { useWebSocketContext } from '../context/WebSocketContext';

const BUTTON_CONFIG = [
  {
    id: 'intervention',
    type: 'intervention',
    label: 'Pomoc w nagłych wypadkach',
    sublabel: 'Upadek, ból, dezorientacja',
    className: 'btn-emergency',
    ariaLabel: 'Rozpocznij interwencję kryzysową'
  },
  {
    id: 'support',
    type: 'support',
    label: 'Wsparcie psychiczne',
    sublabel: 'Rozmowa i pomoc',
    className: 'btn-mental',
    ariaLabel: 'Rozpocznij sesję wsparcia'
  },
  {
    id: 'survey',
    type: 'survey',
    label: 'Uzupełnij profil',
    sublabel: 'Informacje o podopiecznym',
    className: 'btn-survey',
    ariaLabel: 'Uzupełnij profil podopiecznego'
  }
];

/**
 * @param {Object} props
 * @param {Function} [props.scrollToChat] - Function to scroll to chat section
 */
const ActionButtons = ({ scrollToChat }) => {
  const { isConnected, conversationType, startConversation } = useWebSocketContext();

  const handleButtonClick = (type) => {
    if (!isConnected) return;

    const success = startConversation(type);

    if (success && scrollToChat) {
      setTimeout(() => {
        scrollToChat();
      }, 100);
    }
  };

  return (
    <div className="action-buttons" role="group" aria-label="Akcje asystenta">
      {BUTTON_CONFIG.map(({ id, type, label, sublabel, className, ariaLabel }) => {
        const isActive = conversationType === type;
        const isDisabled = !isConnected;

        return (
          <button
            key={id}
            className={`btn-large ${className} ${isActive ? 'active' : ''}`}
            onClick={() => handleButtonClick(type)}
            disabled={isDisabled}
            aria-label={ariaLabel}
            aria-pressed={isActive}
            title={isDisabled ? 'Brak połączenia z serwerem' : label}
          >
            <span className="btn-label">{label}</span>
            <span className="btn-sublabel">{sublabel}</span>
            {isActive && (
              <span className="btn-active-indicator" aria-hidden="true">
                Aktywne
              </span>
            )}
          </button>
        );
      })}

      {/* Connection warning */}
      {!isConnected && (
        <p className="action-buttons-warning" role="alert">
          Brak połączenia z serwerem. Przyciski są nieaktywne.
        </p>
      )}
    </div>
  );
};

export default ActionButtons;
