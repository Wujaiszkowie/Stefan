import React, { useRef, useState } from 'react';
import { WebSocketProvider, useWebSocketContext } from './context/WebSocketContext';
import Header from './components/Header';
import EventList from './components/EventList';
import Calendar from './components/Calendar';
import NavRail from './components/NavRail';
import Chat from './components/Chat';
import ConnectionStatus from './components/ConnectionStatus';
import { ConnectionSnackbar } from './components/Snackbar';

function AppContent() {
  const chatRef = useRef(null);
  const [activeTab, setActiveTab] = useState('home');
  const { conversationType } = useWebSocketContext();

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

      {/* Connection Snackbar (M3) */}
      <ConnectionSnackbar />
    </div>
  );
}

function App() {
  return (
    <WebSocketProvider>
      <AppContent />
    </WebSocketProvider>
  );
}

export default App;
