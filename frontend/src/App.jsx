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
  const { isChecking, showSurveyModal } = useOnboarding();

  // Don't show main chat when survey modal is open, or when conversation is 'survey'
  const showMainChat = conversationType && conversationType !== 'survey' && !showSurveyModal;

  const scrollToChat = () => {
    chatRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  const handleTabChange = (tabId) => {
    setActiveTab(tabId);
    if (tabId === 'home') {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  // Show loading state while checking onboarding status
  if (isChecking) {
    return (
      <div className="app-loading">
        <div className="loading-spinner"></div>
        <p>Ładowanie...</p>
      </div>
    );
  }

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

          {/* Chat Section - only visible when a non-survey conversation is active */}
          {showMainChat && (
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
