# Task 11: Handle Conversation Completion and Transitions

## Phase
Phase 4: Polish & Cleanup

## Priority
Medium - UX improvement

## Description
Implement proper handling of conversation completion events and transitions between conversation types. Show completion summaries and allow smooth switching between flows.

## Acceptance Criteria
- [ ] Handle `*_completed` messages (survey, intervention, support)
- [ ] Reset conversation type on completion
- [ ] Show completion summary in chat
- [ ] Allow starting new conversation after completion
- [ ] Confirm before switching mid-conversation
- [ ] Handle abrupt conversation endings gracefully

## Implementation

### Modify: `src/context/WebSocketContext.jsx`

Enhance conversation management:

```javascript
// Add state for completion tracking
const [lastCompletion, setLastCompletion] = useState(null);

// Enhanced completion handlers
useEffect(() => {
  const handleCompletion = (type) => (msg) => {
    console.log(`[WebSocketContext] ${type} completed`, msg.payload);
    setLastCompletion({
      type,
      payload: msg.payload,
      timestamp: Date.now()
    });
    setConversationType(null);
  };

  const unsubscribes = [
    subscribe('survey_completed', handleCompletion('survey')),
    subscribe('intervention_completed', handleCompletion('intervention')),
    subscribe('support_completed', handleCompletion('support'))
  ];

  return () => unsubscribes.forEach(unsub => unsub());
}, [subscribe]);

/**
 * Start a conversation flow (with confirmation for mid-conversation switch)
 * @param {ConversationType} type - Conversation type to start
 * @param {Object} options - Options
 * @param {boolean} options.force - Skip confirmation
 * @returns {boolean|'confirm_needed'}
 */
const startConversation = useCallback((type, options = {}) => {
  if (!isConnected) {
    console.warn('[WebSocketContext] Cannot start conversation - not connected');
    return false;
  }

  // If switching mid-conversation and force is not set
  if (conversationType && conversationType !== type && !options.force) {
    return 'confirm_needed';
  }

  // End any existing conversation
  if (conversationType) {
    sendMessage(`${conversationType}_complete`, {});
  }

  // Start new conversation
  setConversationType(type);
  setLastCompletion(null);
  sendMessage(`${type}_start`, options.payload || {});
  return true;
}, [isConnected, conversationType, sendMessage]);

// Add to context value
const value = {
  // ... existing values ...
  lastCompletion,
  clearLastCompletion: () => setLastCompletion(null)
};
```

### Modify: `src/components/ActionButtons.jsx`

Add confirmation dialog for mid-conversation switch:

```javascript
import React, { useState } from 'react';
import { useWebSocketContext } from '../context/WebSocketContext';

const ActionButtons = ({ scrollToChat }) => {
  const { isConnected, conversationType, startConversation } = useWebSocketContext();
  const [pendingAction, setPendingAction] = useState(null);

  const handleButtonClick = (type) => {
    if (!isConnected) return;

    const result = startConversation(type);

    if (result === 'confirm_needed') {
      setPendingAction(type);
    } else if (result && scrollToChat) {
      setTimeout(() => scrollToChat(), 100);
    }
  };

  const confirmSwitch = () => {
    if (pendingAction) {
      startConversation(pendingAction, { force: true });
      setPendingAction(null);
      if (scrollToChat) {
        setTimeout(() => scrollToChat(), 100);
      }
    }
  };

  const cancelSwitch = () => {
    setPendingAction(null);
  };

  return (
    <>
      <div className="action-buttons" role="group" aria-label="Akcje asystenta">
        {/* ... existing buttons ... */}
      </div>

      {/* Confirmation Dialog */}
      {pendingAction && (
        <div className="action-confirm-overlay" role="dialog" aria-modal="true">
          <div className="action-confirm-dialog">
            <p>Masz aktywną rozmowę. Czy chcesz ją zakończyć i rozpocząć nową?</p>
            <div className="action-confirm-buttons">
              <button
                className="btn-confirm-cancel"
                onClick={cancelSwitch}
              >
                Anuluj
              </button>
              <button
                className="btn-confirm-proceed"
                onClick={confirmSwitch}
                autoFocus
              >
                Zakończ i rozpocznij
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};
```

### Add Completion Summary Component

Create `src/components/CompletionSummary.jsx`:

```javascript
/**
 * Completion Summary Component
 *
 * Displays summary after conversation completion.
 */

import React from 'react';
import { useWebSocketContext } from '../context/WebSocketContext';

const COMPLETION_MESSAGES = {
  survey: {
    title: 'Profil zaktualizowany',
    icon: '✅',
    getMessage: (payload) =>
      `Zapisano ${payload.factsSavedCount || 0} informacji o podopiecznym.`
  },
  intervention: {
    title: 'Interwencja zakończona',
    icon: '🏥',
    getMessage: (payload) =>
      payload.scenarioId
        ? `Obsłużono scenariusz: ${payload.scenarioId}`
        : 'Sytuacja została obsłużona.'
  },
  support: {
    title: 'Sesja zakończona',
    icon: '💬',
    getMessage: (payload) => {
      if (payload.duration) {
        const minutes = Math.round(payload.duration / 60);
        return `Czas trwania sesji: ${minutes} min.`;
      }
      return 'Dziękujemy za rozmowę.';
    }
  }
};

function CompletionSummary() {
  const { lastCompletion, clearLastCompletion } = useWebSocketContext();

  if (!lastCompletion) return null;

  const config = COMPLETION_MESSAGES[lastCompletion.type];
  if (!config) return null;

  return (
    <div className="completion-summary" role="status">
      <div className="completion-icon">{config.icon}</div>
      <div className="completion-content">
        <h4>{config.title}</h4>
        <p>{config.getMessage(lastCompletion.payload)}</p>
      </div>
      <button
        className="completion-dismiss"
        onClick={clearLastCompletion}
        aria-label="Zamknij"
      >
        ×
      </button>
    </div>
  );
}

export default CompletionSummary;
```

### CSS Additions

```css
/* Confirmation Dialog Overlay */
.action-confirm-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.action-confirm-dialog {
  background: white;
  padding: 24px;
  border-radius: 12px;
  max-width: 400px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.action-confirm-dialog p {
  margin: 0 0 16px;
  font-size: 14px;
  color: #374151;
}

.action-confirm-buttons {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn-confirm-cancel {
  padding: 8px 16px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  color: #374151;
  cursor: pointer;
}

.btn-confirm-cancel:hover {
  background: #f3f4f6;
}

.btn-confirm-proceed {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  background: #1d4ed8;
  color: white;
  cursor: pointer;
}

.btn-confirm-proceed:hover {
  background: #1e40af;
}

/* Completion Summary */
.completion-summary {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: #f0fdf4;
  border: 1px solid #86efac;
  border-radius: 12px;
  margin-bottom: 16px;
}

.completion-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.completion-content {
  flex: 1;
}

.completion-content h4 {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 600;
  color: #166534;
}

.completion-content p {
  margin: 0;
  font-size: 13px;
  color: #15803d;
}

.completion-dismiss {
  background: none;
  border: none;
  font-size: 20px;
  color: #86efac;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.completion-dismiss:hover {
  color: #166534;
}
```

### Integrate into Chat.jsx

```javascript
import CompletionSummary from './CompletionSummary';

const Chat = () => {
  // ... existing code ...

  return (
    <div className="chat-container">
      {/* Show completion summary if present */}
      <CompletionSummary />

      {/* Conversation Type Indicator */}
      {conversationType && (
        // ... existing code ...
      )}

      {/* ... rest of component ... */}
    </div>
  );
};
```

## Conversation Lifecycle

```
[No conversation]
      │
      ▼
User clicks action button
      │
      ├── First conversation: start immediately
      │
      └── Mid-conversation: show confirmation dialog
            │
            ├── Cancel → stay in current conversation
            │
            └── Confirm → end current, start new
                  │
                  ▼
      [Conversation active]
            │
            ▼
      Backend sends *_completed
            │
            ▼
      Context: conversationType = null
      Context: lastCompletion = { type, payload }
            │
            ▼
      [Completion summary shown]
            │
            ▼
      User dismisses or starts new conversation
```

## Testing Checklist

- [ ] Survey completion shows fact count
- [ ] Intervention completion shows scenario
- [ ] Support completion shows duration
- [ ] Completion summary dismissible
- [ ] Mid-conversation switch shows confirmation
- [ ] Cancel confirmation keeps current conversation
- [ ] Confirm ends current and starts new
- [ ] Dialog accessible via keyboard

## Dependencies
- Task 05 (WebSocket context)
- Task 08 (Chat component)
- Task 09 (Action buttons)

## Estimated Effort
40-50 minutes

## Files to Create
- `src/components/CompletionSummary.jsx`

## Files to Modify
- `src/context/WebSocketContext.jsx`
- `src/components/ActionButtons.jsx`
- `src/components/Chat.jsx`
- `src/index.css`
