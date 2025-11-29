# Task 09: Modify ActionButtons.jsx for WebSocket Flows

## Phase
Phase 3: Component Integration

## Priority
High - User interaction entry point

## Description
Refactor ActionButtons component to trigger WebSocket conversation flows. Add third button for survey, integrate with WebSocket context, and add scroll-to-chat functionality.

## Acceptance Criteria
- [ ] Three buttons: Emergency, Mental Support, Survey
- [ ] Each button starts corresponding WebSocket flow
- [ ] Buttons disabled when not connected
- [ ] Visual feedback for active conversation type
- [ ] Clicking button scrolls to chat
- [ ] Polish language labels
- [ ] Accessible (proper ARIA attributes)

## Current State

```javascript
// src/components/ActionButtons.jsx (current)
const ActionButtons = () => {
  return (
    <div className="action-buttons">
      <button className="btn-large btn-emergency">
        <span>Emergency</span>
        <span>Help</span>
      </button>
      <button className="btn-large btn-mental">
        <span>I need mental</span>
        <span>support</span>
      </button>
    </div>
  );
};
```

Issues:
1. No click handlers
2. English labels (should be Polish)
3. No survey button
4. No connection awareness
5. No scroll-to-chat

## Implementation

### File: `src/components/ActionButtons.jsx` (refactored)

```javascript
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
    icon: '🚨',
    ariaLabel: 'Rozpocznij interwencję kryzysową'
  },
  {
    id: 'support',
    type: 'support',
    label: 'Wsparcie psychiczne',
    sublabel: 'Rozmowa i pomoc',
    className: 'btn-mental',
    icon: '💬',
    ariaLabel: 'Rozpocznij sesję wsparcia'
  },
  {
    id: 'survey',
    type: 'survey',
    label: 'Uzupełnij profil',
    sublabel: 'Informacje o podopiecznym',
    className: 'btn-survey',
    icon: '📋',
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
      // Small delay to allow state update before scroll
      setTimeout(() => {
        scrollToChat();
      }, 100);
    }
  };

  return (
    <div className="action-buttons" role="group" aria-label="Akcje asystenta">
      {BUTTON_CONFIG.map(({ id, type, label, sublabel, className, icon, ariaLabel }) => {
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
            <span className="btn-icon" aria-hidden="true">{icon}</span>
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
```

### CSS Additions (add to `src/index.css`)

```css
/* Action Buttons Container */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-buttons[role="group"] {
  margin-top: 16px;
}

/* Large Action Button */
.btn-large {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px;
  border: 2px solid transparent;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  min-height: 100px;
}

.btn-large:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-large:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.btn-large:not(:disabled):active {
  transform: translateY(0);
}

/* Button Icon */
.btn-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

/* Button Labels */
.btn-label {
  font-size: 14px;
  font-weight: 600;
  text-align: center;
}

.btn-sublabel {
  font-size: 11px;
  opacity: 0.8;
  margin-top: 4px;
  text-align: center;
}

/* Emergency Button */
.btn-emergency {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #dc2626;
  border-color: #fca5a5;
}

.btn-emergency:not(:disabled):hover {
  background: linear-gradient(135deg, #fecaca 0%, #fca5a5 100%);
}

.btn-emergency.active {
  border-color: #dc2626;
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.2);
}

/* Mental Support Button */
.btn-mental {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #1d4ed8;
  border-color: #93c5fd;
}

.btn-mental:not(:disabled):hover {
  background: linear-gradient(135deg, #bfdbfe 0%, #93c5fd 100%);
}

.btn-mental.active {
  border-color: #1d4ed8;
  box-shadow: 0 0 0 3px rgba(29, 78, 216, 0.2);
}

/* Survey Button */
.btn-survey {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  color: #16a34a;
  border-color: #86efac;
}

.btn-survey:not(:disabled):hover {
  background: linear-gradient(135deg, #bbf7d0 0%, #86efac 100%);
}

.btn-survey.active {
  border-color: #16a34a;
  box-shadow: 0 0 0 3px rgba(22, 163, 74, 0.2);
}

/* Active Indicator */
.btn-active-indicator {
  position: absolute;
  top: 8px;
  right: 8px;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.1);
  font-weight: 500;
}

/* Warning Message */
.action-buttons-warning {
  margin-top: 8px;
  padding: 8px 12px;
  background: #fef3c7;
  color: #92400e;
  border-radius: 8px;
  font-size: 12px;
  text-align: center;
}
```

## Key Changes Summary

| Aspect | Before | After |
|--------|--------|-------|
| Buttons | 2 | 3 (+ survey) |
| Language | English | Polish |
| Handlers | None | WebSocket flow triggers |
| Connection aware | No | Yes (disabled when offline) |
| Active state | No | Visual feedback |
| Accessibility | Basic | Full ARIA support |
| Scroll to chat | No | Yes |

## Button → Action Mapping

| Button | WebSocket Message | Payload |
|--------|------------------|---------|
| Pomoc w nagłych wypadkach | `intervention_start` | `{}` |
| Wsparcie psychiczne | `support_start` | `{}` |
| Uzupełnij profil | `survey_start` | `{}` |

## User Flow

```
User clicks "Pomoc w nagłych wypadkach"
    │
    ├── Check: isConnected? No → Button disabled, no action
    │
    └── Yes → startConversation('intervention')
              │
              ├── Context: end previous conversation (if any)
              │
              ├── Context: set conversationType = 'intervention'
              │
              ├── Context: send 'intervention_start' message
              │
              └── scrollToChat() → smooth scroll to Chat section
```

## Testing Checklist

- [ ] All three buttons render
- [ ] Polish labels display correctly
- [ ] Buttons disabled when disconnected
- [ ] Warning message shows when disconnected
- [ ] Click triggers correct flow start
- [ ] Active button has visual indicator
- [ ] Clicking scrolls to chat section
- [ ] Keyboard accessible (Tab, Enter)
- [ ] Screen reader announces button purpose

## Accessibility Notes

- `role="group"` groups related buttons
- `aria-label` on container and each button
- `aria-pressed` indicates active state
- `aria-hidden` on decorative icons
- `role="alert"` on warning message for screen reader announcement
- `title` provides tooltip for disabled state explanation

## Dependencies
- Task 05 (WebSocket context)
- Task 07 (App wrapper with scrollToChat prop)

## Estimated Effort
30-40 minutes

## Files to Modify
- `src/components/ActionButtons.jsx` (refactor)
- `src/index.css` (add styles)
