# Task 08: Refactor Chat.jsx for WebSocket

## Phase
Phase 3: Component Integration

## Priority
High - Core functionality change

## Description
Major refactoring of Chat component to replace HTTP fetch with WebSocket communication. Support multiple conversation types and handle all relevant message types from backend.

## Acceptance Criteria
- [ ] Remove HTTP fetch call
- [ ] Use WebSocket via `useWebSocketContext`
- [ ] Support three conversation types (support, survey, intervention)
- [ ] Subscribe to all relevant message types
- [ ] Send messages based on active conversation type
- [ ] Disable input when not connected or no active conversation
- [ ] Display conversation type indicator
- [ ] Handle error messages in Polish

## Current State Analysis

```javascript
// Current: HTTP fetch to wrong port
const response = await fetch('http://localhost:8000/api/v1/chat', {
  method: 'POST',
  // ...
});
```

Issues:
1. Wrong port (8000 vs 8080)
2. HTTP instead of WebSocket
3. No conversation type awareness
4. No session management

## Implementation

### File: `src/components/Chat.jsx` (refactored)

```javascript
/**
 * Chat Component
 *
 * Real-time chat interface using WebSocket.
 * Supports multiple conversation types: support, survey, intervention.
 */

import React, { useState, useRef, useEffect } from 'react';
import { useWebSocketContext } from '../context/WebSocketContext';

// Conversation type display names (Polish)
const CONVERSATION_LABELS = {
  support: 'Wsparcie psychiczne',
  survey: 'Uzupełnianie profilu',
  intervention: 'Pomoc w nagłych wypadkach'
};

// Message type to conversation type mapping
const MESSAGE_TYPE_MAP = {
  support_message: 'support',
  survey_question: 'survey',
  intervention_question: 'intervention',
  intervention_scenario_matched: 'intervention'
};

const Chat = () => {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);

  const {
    isConnected,
    conversationType,
    sendChatMessage,
    subscribe
  } = useWebSocketContext();

  // Auto-scroll to bottom on new messages
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // Subscribe to all relevant message types
  useEffect(() => {
    const addAssistantMessage = (content) => {
      setLoading(false);
      setMessages(prev => [...prev, {
        id: Date.now(),
        role: 'assistant',
        content
      }]);
    };

    const unsubscribes = [
      // Support messages
      subscribe('support_message', (msg) => {
        addAssistantMessage(msg.payload.message);
      }),

      // Survey questions
      subscribe('survey_question', (msg) => {
        const stepLabel = msg.payload.step ? ` (${msg.payload.step})` : '';
        addAssistantMessage(msg.payload.question + stepLabel);
      }),

      // Intervention messages
      subscribe('intervention_question', (msg) => {
        addAssistantMessage(msg.payload.question);
      }),

      subscribe('intervention_scenario_matched', (msg) => {
        const { scenario, severity } = msg.payload;
        addAssistantMessage(
          `Rozpoznano sytuację: ${scenario} (poziom: ${severity})`
        );
      }),

      // Completion messages
      subscribe('survey_completed', (msg) => {
        setLoading(false);
        addAssistantMessage(
          `Profil został zapisany. Zapisano ${msg.payload.factsSavedCount || 0} informacji.`
        );
      }),

      subscribe('intervention_completed', (msg) => {
        setLoading(false);
        addAssistantMessage('Interwencja zakończona. Czy potrzebujesz dalszej pomocy?');
      }),

      subscribe('support_completed', (msg) => {
        setLoading(false);
        const duration = msg.payload.duration
          ? ` Czas trwania: ${Math.round(msg.payload.duration / 60)} min.`
          : '';
        addAssistantMessage(`Sesja wsparcia zakończona.${duration}`);
      }),

      // Error handling
      subscribe('error', (msg) => {
        setLoading(false);
        setMessages(prev => [...prev, {
          id: Date.now(),
          role: 'assistant',
          content: `Błąd: ${msg.payload.message}`,
          isError: true
        }]);
      })
    ];

    return () => unsubscribes.forEach(unsub => unsub());
  }, [subscribe]);

  // Send message handler
  const sendMessage = () => {
    if (!input.trim() || loading || !isConnected) return;

    if (!conversationType) {
      // No active conversation - show hint
      setMessages(prev => [...prev, {
        id: Date.now(),
        role: 'assistant',
        content: 'Wybierz typ rozmowy, klikając jeden z przycisków powyżej.',
        isSystem: true
      }]);
      return;
    }

    const userMessage = {
      id: Date.now(),
      role: 'user',
      content: input.trim()
    };

    setMessages(prev => [...prev, userMessage]);
    setInput('');
    setLoading(true);

    // Send via WebSocket
    const success = sendChatMessage(userMessage.content);

    if (!success) {
      setLoading(false);
      setMessages(prev => [...prev, {
        id: Date.now() + 1,
        role: 'assistant',
        content: 'Nie udało się wysłać wiadomości. Sprawdź połączenie.',
        isError: true
      }]);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  // Clear chat when conversation type changes
  useEffect(() => {
    if (conversationType) {
      setMessages([]);
      setLoading(true); // Wait for first message from backend
    }
  }, [conversationType]);

  const isInputDisabled = !isConnected || loading;
  const placeholder = !isConnected
    ? 'Brak połączenia...'
    : !conversationType
      ? 'Wybierz typ rozmowy...'
      : 'Wpisz wiadomość...';

  return (
    <div className="chat-container">
      {/* Conversation Type Indicator */}
      {conversationType && (
        <div className="chat-conversation-type">
          <span className="conversation-badge">
            {CONVERSATION_LABELS[conversationType]}
          </span>
        </div>
      )}

      <div className="chat-messages">
        {messages.length === 0 && !conversationType && (
          <div className="chat-empty-state">
            <p>Rozpocznij rozmowę wybierając jedną z opcji:</p>
            <ul style={{ textAlign: 'left', marginTop: '12px' }}>
              <li><strong>Pomoc w nagłych wypadkach</strong> - w sytuacjach kryzysowych</li>
              <li><strong>Wsparcie psychiczne</strong> - rozmowa i wsparcie</li>
              <li><strong>Uzupełnij profil</strong> - informacje o podopiecznym</li>
            </ul>
          </div>
        )}

        {messages.length === 0 && conversationType && loading && (
          <div className="chat-empty-state">
            <p>Łączenie z asystentem...</p>
          </div>
        )}

        {messages.map((message) => (
          <div
            key={message.id}
            className={`chat-message ${message.role} ${message.isError ? 'error' : ''} ${message.isSystem ? 'system' : ''}`}
          >
            <div className="message-content">
              {message.content}
            </div>
          </div>
        ))}

        {loading && messages.length > 0 && (
          <div className="chat-message assistant">
            <div className="message-content loading">
              <span className="loading-dot"></span>
              <span className="loading-dot"></span>
              <span className="loading-dot"></span>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      <div className="chat-input-container">
        <textarea
          className="chat-input"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder={placeholder}
          rows="1"
          disabled={isInputDisabled}
        />
        <button
          className="chat-send-button"
          onClick={sendMessage}
          disabled={!input.trim() || isInputDisabled}
          title={!isConnected ? 'Brak połączenia' : 'Wyślij wiadomość'}
        >
          <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <line x1="22" y1="2" x2="11" y2="13"></line>
            <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
          </svg>
        </button>
      </div>
    </div>
  );
};

export default Chat;
```

### CSS Additions (add to `src/index.css`)

```css
/* Conversation Type Badge */
.chat-conversation-type {
  padding: 8px 16px;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
}

.conversation-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 9999px;
  font-size: 12px;
  font-weight: 500;
  background: #dbeafe;
  color: #1d4ed8;
}

/* Error Message Styling */
.chat-message.error .message-content {
  background: #fee2e2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

/* System Message Styling */
.chat-message.system .message-content {
  background: #f3f4f6;
  color: #6b7280;
  font-style: italic;
}

/* Empty State List */
.chat-empty-state ul {
  list-style: disc;
  padding-left: 20px;
}

.chat-empty-state li {
  margin-bottom: 8px;
}
```

## Key Changes Summary

| Aspect | Before | After |
|--------|--------|-------|
| Communication | HTTP fetch | WebSocket |
| Endpoint | localhost:8000 | ws://localhost:8080/ws |
| Message types | Single | Multiple (support, survey, intervention) |
| State management | Local | Context-based |
| Error handling | Generic | Polish messages |
| Empty state | Generic | Instructional |

## Message Flow

```
User sends message
    │
    ├── Check: isConnected? No → Show error
    │
    ├── Check: conversationType? No → Show hint
    │
    └── Yes → sendChatMessage(content)
              │
              └── Context sends: {type}_message
                  │
                  └── Backend responds: {type}_message / error
                      │
                      └── Subscription handler → addAssistantMessage
```

## Testing Checklist

- [ ] Chat renders without errors
- [ ] Messages display correctly (user/assistant)
- [ ] Loading indicator shows during wait
- [ ] Error messages display in red
- [ ] Input disabled when disconnected
- [ ] Input disabled when no conversation active
- [ ] Conversation badge shows correct type
- [ ] Empty state shows helpful instructions
- [ ] Chat clears when conversation type changes
- [ ] Enter key sends message
- [ ] Scroll to bottom on new message

## Dependencies
- Task 05 (WebSocket context)

## Estimated Effort
45-60 minutes

## Files to Modify
- `src/components/Chat.jsx` (major refactor)
- `src/index.css` (add styles)
