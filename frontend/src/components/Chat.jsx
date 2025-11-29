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
    const addAssistantMessage = (content, sessionType, options = {}) => {
      setLoading(false);
      setMessages(prev => [...prev, {
        id: Date.now(),
        role: 'assistant',
        content,
        sessionType,
        ...options
      }]);
    };

    const unsubscribes = [
      // Support messages
      subscribe('support_message', (msg) => {
        console.log('[Chat] Received support_message:', msg);
        // Backend sends 'text' field, fallback to 'message' for compatibility
        const content = msg.payload.text || msg.payload.message;
        if (content) {
          addAssistantMessage(content, 'support');
        }
      }),

      // Survey questions
      subscribe('survey_question', (msg) => {
        addAssistantMessage(msg.payload.question, 'survey');
      }),

      // Intervention messages
      subscribe('intervention_question', (msg) => {
        addAssistantMessage(msg.payload.question, 'intervention');
      }),

      subscribe('intervention_scenario_matched', (msg) => {
        const { scenario, severity } = msg.payload;
        addAssistantMessage(
          `Rozpoznano sytuację: ${scenario} (poziom: ${severity})`,
          'intervention'
        );
      }),

      // Completion messages
      subscribe('survey_completed', (msg) => {
        setLoading(false);
        addAssistantMessage(
          `Profil został zapisany. Zapisano ${msg.payload.factsSavedCount || 0} informacji.`,
          'survey'
        );
      }),

      subscribe('intervention_completed', (msg) => {
        setLoading(false);
        addAssistantMessage('Interwencja zakończona. Czy potrzebujesz dalszej pomocy?', 'intervention');
      }),

      subscribe('support_completed', (msg) => {
        setLoading(false);
        const duration = msg.payload.duration
          ? ` Czas trwania: ${Math.round(msg.payload.duration / 60)} min.`
          : '';
        addAssistantMessage(`Sesja wsparcia zakończona.${duration}`, 'support');
      }),

      // Error handling - detect session type from error message
      subscribe('error', (msg) => {
        setLoading(false);
        const errorMsg = msg.payload.message || '';
        let errorSessionType = null;
        if (errorMsg.includes('interwencj')) errorSessionType = 'intervention';
        else if (errorMsg.includes('wsparci') || errorMsg.includes('support')) errorSessionType = 'support';
        else if (errorMsg.includes('ankiet') || errorMsg.includes('survey')) errorSessionType = 'survey';
        addAssistantMessage(`Błąd: ${errorMsg}`, errorSessionType, { isError: true });
      })
    ];

    return () => unsubscribes.forEach(unsub => unsub());
  }, [subscribe]);

  // Clear chat when conversation type changes
  useEffect(() => {
    if (conversationType) {
      setMessages([]);
      setLoading(true);
    }
  }, [conversationType]);

  // Filter messages to show only those matching current session type
  const filteredMessages = messages.filter(msg => {
    // Always show user messages
    if (msg.role === 'user') return true;
    // Show messages without sessionType (generic messages)
    if (!msg.sessionType) return true;
    // Only show assistant messages matching current conversation
    return msg.sessionType === conversationType;
  });

  // Send message handler
  const sendMessage = () => {
    if (!input.trim() || loading || !isConnected) return;

    if (!conversationType) {
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

    console.log('[Chat] Sending message:', userMessage.content, 'conversationType:', conversationType);
    const success = sendChatMessage(userMessage.content);
    console.log('[Chat] Send result:', success);

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
        {filteredMessages.length === 0 && !conversationType && (
          <div className="chat-empty-state">
            <div>
              <p>Rozpocznij rozmowę wybierając jedną z opcji:</p>
              <ul className="chat-options-list">
                <li><strong>Pomoc w nagłych wypadkach</strong> - w sytuacjach kryzysowych</li>
                <li><strong>Wsparcie psychiczne</strong> - rozmowa i wsparcie</li>
                <li><strong>Uzupełnij profil</strong> - informacje o podopiecznym</li>
              </ul>
            </div>
          </div>
        )}

        {filteredMessages.length === 0 && conversationType && loading && (
          <div className="chat-empty-state">
            <p>Łączenie z asystentem...</p>
          </div>
        )}

        {filteredMessages.map((message) => (
          <div
            key={message.id}
            className={`chat-message ${message.role} ${message.isError ? 'error' : ''} ${message.isSystem ? 'system' : ''}`}
          >
            <div className="message-content">
              {message.content}
            </div>
          </div>
        ))}

        {loading && filteredMessages.length > 0 && (
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
