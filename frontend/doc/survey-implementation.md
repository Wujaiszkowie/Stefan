# Survey Onboarding Implementation Plan

## Cel

Implementacja mechanizmu sprawdzania, czy użytkownik przeszedł onboarding (posiada zapisane fakty). Jeśli nie ma faktów, wyświetlamy modal z ankietą Survey do zebrania podstawowych informacji o podopiecznym.

---

## Backend API

**Endpoint:** `HEAD /api/fact`

| Status | Znaczenie |
|--------|-----------|
| `200 OK` | Fakty istnieją (onboarding zakończony) |
| `204 No Content` | Brak faktów (wymaga onboardingu) |

```java
@HEAD
public Response facts() {
    return factRepository.hasAnyFacts()
        ? Response.ok().build()
        : Response.noContent().build();
}
```

---

## Plan implementacji

### Faza 1: REST API Client

#### Task 1.1: Utworzenie serwisu API (`src/services/api.js`)

```javascript
/**
 * API Service
 * REST API client for backend communication.
 */

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

/**
 * Check if user has completed onboarding (has facts).
 * @returns {Promise<boolean>} true if onboarded, false otherwise
 */
export async function checkOnboardingStatus() {
  try {
    const response = await fetch(`${API_BASE_URL}/api/fact`, {
      method: 'HEAD',
    });

    // 200 = has facts (onboarded), 204 = no facts (needs onboarding)
    return response.status === 200;
  } catch (error) {
    console.error('[API] Failed to check onboarding status:', error);
    // On error, assume onboarded to not block user
    return true;
  }
}

export default {
  checkOnboardingStatus,
};
```

---

### Faza 2: Kontekst Onboardingu

#### Task 2.1: Utworzenie OnboardingContext (`src/context/OnboardingContext.jsx`)

```javascript
/**
 * Onboarding Context Provider
 *
 * Manages onboarding state - checks if user has completed initial survey.
 * Shows survey modal if onboarding is not completed.
 */

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { checkOnboardingStatus } from '../services/api';

const OnboardingContext = createContext(null);

export function OnboardingProvider({ children }) {
  const [isOnboarded, setIsOnboarded] = useState(null); // null = checking, true/false = result
  const [isChecking, setIsChecking] = useState(true);
  const [showSurveyModal, setShowSurveyModal] = useState(false);

  // Check onboarding status on mount
  useEffect(() => {
    async function checkStatus() {
      setIsChecking(true);
      const status = await checkOnboardingStatus();
      setIsOnboarded(status);
      setIsChecking(false);

      // Auto-show modal if not onboarded
      if (!status) {
        setShowSurveyModal(true);
      }
    }

    checkStatus();
  }, []);

  const openSurveyModal = useCallback(() => {
    setShowSurveyModal(true);
  }, []);

  const closeSurveyModal = useCallback(() => {
    setShowSurveyModal(false);
  }, []);

  const markAsOnboarded = useCallback(() => {
    setIsOnboarded(true);
    setShowSurveyModal(false);
  }, []);

  const value = {
    isOnboarded,
    isChecking,
    showSurveyModal,
    openSurveyModal,
    closeSurveyModal,
    markAsOnboarded,
  };

  return (
    <OnboardingContext.Provider value={value}>
      {children}
    </OnboardingContext.Provider>
  );
}

export function useOnboarding() {
  const context = useContext(OnboardingContext);
  if (!context) {
    throw new Error('useOnboarding must be used within an OnboardingProvider');
  }
  return context;
}

export default OnboardingContext;
```

---

### Faza 3: Komponent Modal

#### Task 3.1: Utworzenie SurveyModal (`src/components/SurveyModal.jsx`)

```jsx
/**
 * Survey Modal Component
 *
 * Modal dialog for onboarding survey.
 * Contains embedded Chat component configured for survey mode.
 */

import React, { useEffect } from 'react';
import { useWebSocketContext } from '../context/WebSocketContext';
import { useOnboarding } from '../context/OnboardingContext';
import Chat from './Chat';

const SurveyModal = () => {
  const { showSurveyModal, closeSurveyModal, markAsOnboarded } = useOnboarding();
  const {
    startConversation,
    subscribe,
    isConnected,
    conversationType
  } = useWebSocketContext();

  // Auto-start survey when modal opens and connected
  useEffect(() => {
    if (showSurveyModal && isConnected && conversationType !== 'survey') {
      startConversation('survey');
    }
  }, [showSurveyModal, isConnected, conversationType, startConversation]);

  // Listen for survey completion
  useEffect(() => {
    if (!showSurveyModal) return;

    const unsubscribe = subscribe('survey_completed', (msg) => {
      console.log('[SurveyModal] Survey completed', msg.payload);
      markAsOnboarded();
    });

    return () => unsubscribe();
  }, [showSurveyModal, subscribe, markAsOnboarded]);

  // Handle escape key
  useEffect(() => {
    const handleEscape = (e) => {
      if (e.key === 'Escape') {
        closeSurveyModal();
      }
    };

    if (showSurveyModal) {
      document.addEventListener('keydown', handleEscape);
      document.body.style.overflow = 'hidden'; // Prevent background scroll
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
          <button
            className="modal-close-btn"
            onClick={closeSurveyModal}
            aria-label="Zamknij"
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
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

#### Task 3.2: Style CSS dla modalu (dodanie do `index.css`)

```css
/* ============================================
   Modal Styles (M3)
   ============================================ */

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 24px;
  animation: fadeIn var(--md-sys-motion-duration-medium) var(--md-sys-motion-easing-emphasized);
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-container {
  background: var(--md-sys-color-surface);
  border-radius: var(--md-sys-shape-corner-extra-large);
  box-shadow: var(--md-sys-elevation-3);
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: slideUp var(--md-sys-motion-duration-medium) var(--md-sys-motion-easing-emphasized-decelerate);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(24px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 24px 16px;
  border-bottom: 1px solid var(--md-sys-color-outline-variant);
}

.modal-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 500;
  color: var(--md-sys-color-on-surface);
}

.modal-close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: none;
  background: transparent;
  border-radius: var(--md-sys-shape-corner-full);
  cursor: pointer;
  color: var(--md-sys-color-on-surface-variant);
  transition: background var(--md-sys-motion-duration-short);
}

.modal-close-btn:hover {
  background: var(--md-sys-color-surface-variant);
}

.modal-close-btn:focus-visible {
  outline: 2px solid var(--md-sys-color-primary);
  outline-offset: 2px;
}

.modal-body {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: 0 24px 24px;
}

.modal-description {
  margin: 16px 0;
  color: var(--md-sys-color-on-surface-variant);
  font-size: 14px;
  line-height: 1.5;
}

.modal-chat-container {
  flex: 1;
  min-height: 400px;
  max-height: 60vh;
  overflow: hidden;
  border-radius: var(--md-sys-shape-corner-large);
  border: 1px solid var(--md-sys-color-outline-variant);
}

.modal-chat-container .chat-container {
  height: 100%;
  border-radius: 0;
  border: none;
}

/* Survey Modal specific */
.survey-modal {
  max-width: 700px;
}
```

---

### Faza 4: Integracja

#### Task 4.1: Modyfikacja App.jsx

```jsx
import React, { useRef, useState } from 'react';
import { WebSocketProvider, useWebSocketContext } from './context/WebSocketContext';
import { OnboardingProvider, useOnboarding } from './context/OnboardingContext';
import Header from './components/Header';
import EventList from './components/EventList';
import Calendar from './components/Calendar';
import NavRail from './components/NavRail';
import Chat from './components/Chat';
import ConnectionStatus from './components/ConnectionStatus';
import { ConnectionSnackbar } from './components/Snackbar';
import SurveyModal from './components/SurveyModal';

function AppContent() {
  const chatRef = useRef(null);
  const [activeTab, setActiveTab] = useState('home');
  const { conversationType } = useWebSocketContext();
  const { isChecking } = useOnboarding();

  // Show loading state while checking onboarding
  if (isChecking) {
    return (
      <div className="app-loading">
        <div className="loading-spinner"></div>
        <p>Ładowanie...</p>
      </div>
    );
  }

  const scrollToChat = () => {
    chatRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  const handleTabChange = (tabId) => {
    setActiveTab(tabId);
    if (tabId === 'home') {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  return (
    <div className="app-layout">
      {/* Navigation Rail (M3) */}
      <NavRail
        activeTab={activeTab}
        onTabChange={handleTabChange}
        onChatClick={scrollToChat}
      />

      {/* Main Content Wrapper */}
      <div className="main-wrapper">
        <div className="container">
          {/* Connection Status */}
          <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '16px' }}>
            <ConnectionStatus />
          </div>

          <Header scrollToChat={scrollToChat} />

          <div className="content-grid">
            <div className="main-content">
              <EventList />
            </div>
            <div className="sidebar">
              <Calendar />
            </div>
          </div>

          {/* Chat Section - only visible when a conversation is active */}
          {conversationType && (
            <div ref={chatRef} style={{ marginTop: '48px' }}>
              <h2 style={{ fontSize: '24px', marginBottom: '24px' }}>Asystent AI</h2>
              <Chat />
            </div>
          )}
        </div>
      </div>

      {/* Survey Modal for onboarding */}
      <SurveyModal />

      {/* Connection Snackbar (M3) */}
      <ConnectionSnackbar />
    </div>
  );
}

function App() {
  return (
    <WebSocketProvider>
      <OnboardingProvider>
        <AppContent />
      </OnboardingProvider>
    </WebSocketProvider>
  );
}

export default App;
```

#### Task 4.2: Style dla loading state (dodanie do `index.css`)

```css
/* ============================================
   App Loading State
   ============================================ */

.app-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  gap: 16px;
  color: var(--md-sys-color-on-surface-variant);
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid var(--md-sys-color-surface-variant);
  border-top-color: var(--md-sys-color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
```

---

## Diagram przepływu

```
┌─────────────────────────────────────────────────────────────────┐
│                        App Start                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
                   ┌─────────────────────┐
                   │  HEAD /api/fact     │
                   └─────────────────────┘
                              │
              ┌───────────────┴───────────────┐
              │                               │
              ▼                               ▼
      ┌──────────────┐               ┌──────────────┐
      │   200 OK     │               │ 204 No Content│
      │ (has facts)  │               │  (no facts)   │
      └──────────────┘               └──────────────┘
              │                               │
              ▼                               ▼
      ┌──────────────┐               ┌──────────────┐
      │  Dashboard   │               │ Survey Modal │
      │   (normal)   │               │   (chat)     │
      └──────────────┘               └──────────────┘
                                              │
                                              ▼
                                     ┌──────────────┐
                                     │ WebSocket:   │
                                     │ survey_start │
                                     └──────────────┘
                                              │
                                              ▼
                                     ┌──────────────┐
                                     │ User answers │
                                     │  questions   │
                                     └──────────────┘
                                              │
                                              ▼
                                     ┌──────────────────┐
                                     │ survey_completed │
                                     │    (WebSocket)   │
                                     └──────────────────┘
                                              │
                                              ▼
                                     ┌──────────────┐
                                     │ Close modal  │
                                     │ → Dashboard  │
                                     └──────────────┘
```

---

## Lista plików do utworzenia/modyfikacji

| Plik | Akcja | Opis |
|------|-------|------|
| `src/services/api.js` | CREATE | REST API client |
| `src/context/OnboardingContext.jsx` | CREATE | Kontekst onboardingu |
| `src/components/SurveyModal.jsx` | CREATE | Komponent modalu |
| `src/index.css` | MODIFY | Style dla modalu i loadera |
| `src/App.jsx` | MODIFY | Integracja z OnboardingProvider |

---

## Testy manualne

1. **Scenariusz: Nowy użytkownik (brak faktów)**
   - Start aplikacji
   - Oczekiwanie: Modal z Survey otwiera się automatycznie
   - Odpowiedz na pytania
   - Oczekiwanie: Po `survey_completed` modal się zamyka

2. **Scenariusz: Powracający użytkownik (ma fakty)**
   - Start aplikacji
   - Oczekiwanie: Dashboard wyświetla się bez modalu

3. **Scenariusz: Zamknięcie modalu bez ukończenia**
   - Kliknij X lub Escape
   - Oczekiwanie: Modal się zamyka, dashboard widoczny
   - Przy następnym starcie: Modal pojawi się ponownie

4. **Scenariusz: Błąd API**
   - Wyłącz backend
   - Start aplikacji
   - Oczekiwanie: Dashboard wyświetla się (nie blokujemy użytkownika)

---

## Uwagi implementacyjne

1. **Kolejność providerów**: `WebSocketProvider` musi być zewnętrzny względem `OnboardingProvider`, ponieważ modal potrzebuje WebSocket do komunikacji.

2. **Auto-start survey**: Modal automatycznie wywołuje `startConversation('survey')` po połączeniu WebSocket.

3. **Obsługa completion**: Nasłuchujemy `survey_completed` w dwóch miejscach:
   - `WebSocketContext` - czyści `conversationType`
   - `SurveyModal` - wywołuje `markAsOnboarded()` i zamyka modal

4. **Accessibility**: Modal ma `role="dialog"`, `aria-modal="true"`, obsługę klawisza Escape, fokus trap (można dodać).
