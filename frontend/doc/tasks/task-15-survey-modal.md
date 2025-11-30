# Task 15: Survey Modal Component

## Batch: 2 (zadanie 1/2)

## Cel
Utworzenie komponentu modalu dla ankiety onboardingowej z osadzonym chatem Survey.

## Plik
`src/components/SurveyModal.jsx`

## Wymagania

1. **Struktura:**
   - Overlay (tło z blur)
   - Container (modal box)
   - Header z tytułem i przyciskiem X
   - Body z opisem i osadzonym Chat

2. **Funkcjonalność:**
   - Auto-start `survey` conversation po otwarciu + połączeniu WebSocket
   - Nasłuchiwanie `survey_completed` → wywołanie `markAsOnboarded()`
   - Zamknięcie przez X, kliknięcie overlay, lub Escape
   - Blokada scroll tła gdy modal otwarty

3. **Accessibility:**
   - `role="dialog"`, `aria-modal="true"`
   - `aria-labelledby` dla tytułu
   - Obsługa klawisza Escape

## Implementacja

```jsx
import React, { useEffect } from 'react';
import { useWebSocketContext } from '../context/WebSocketContext';
import { useOnboarding } from '../context/OnboardingContext';
import Chat from './Chat';

const SurveyModal = () => {
  const { showSurveyModal, closeSurveyModal, markAsOnboarded } = useOnboarding();
  const { startConversation, subscribe, isConnected, conversationType } = useWebSocketContext();

  // Auto-start survey
  useEffect(() => {
    if (showSurveyModal && isConnected && conversationType !== 'survey') {
      startConversation('survey');
    }
  }, [showSurveyModal, isConnected, conversationType, startConversation]);

  // Listen for completion
  useEffect(() => {
    if (!showSurveyModal) return;
    const unsubscribe = subscribe('survey_completed', () => {
      markAsOnboarded();
    });
    return () => unsubscribe();
  }, [showSurveyModal, subscribe, markAsOnboarded]);

  // Escape key + body scroll lock
  useEffect(() => {
    const handleEscape = (e) => {
      if (e.key === 'Escape') closeSurveyModal();
    };
    if (showSurveyModal) {
      document.addEventListener('keydown', handleEscape);
      document.body.style.overflow = 'hidden';
    }
    return () => {
      document.removeEventListener('keydown', handleEscape);
      document.body.style.overflow = '';
    };
  }, [showSurveyModal, closeSurveyModal]);

  if (!showSurveyModal) return null;

  return (
    <div className="modal-overlay" onClick={closeSurveyModal}>
      <div
        className="modal-container survey-modal"
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="survey-modal-title"
      >
        <div className="modal-header">
          <h2 id="survey-modal-title">Uzupełnij profil podopiecznego</h2>
          <button className="modal-close-btn" onClick={closeSurveyModal} aria-label="Zamknij">
            <svg>...</svg>
          </button>
        </div>
        <div className="modal-body">
          <p className="modal-description">
            Odpowiedz na kilka pytań, abyśmy mogli lepiej poznać potrzeby podopiecznego.
          </p>
          <div className="modal-chat-container">
            <Chat />
          </div>
        </div>
      </div>
    </div>
  );
};

export default SurveyModal;
```

## Testy manualne

- [ ] Modal otwiera się gdy `showSurveyModal` = true
- [ ] Auto-start survey po połączeniu WebSocket
- [ ] Zamknięcie przez X, overlay click, Escape
- [ ] `survey_completed` → modal się zamyka
- [ ] Scroll tła zablokowany gdy modal otwarty

## Zależności
- Task 14: `OnboardingContext`
- `WebSocketContext`
- `Chat` component

## Następne zadanie
Task 16: Modal CSS Styles
