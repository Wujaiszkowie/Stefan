import React, { useRef } from 'react';
import { WebSocketProvider, useWebSocketContext } from './context/WebSocketContext';
import Header from './components/Header';
import EventList from './components/EventList';
import Calendar from './components/Calendar';
import ActionButtons from './components/ActionButtons';
import TopNav from './components/TopNav';
import Chat from './components/Chat';
import ConnectionStatus from './components/ConnectionStatus';

function AppContent() {
  const chatRef = useRef(null);
  const { conversationType } = useWebSocketContext();

  const scrollToChat = () => {
    chatRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  return (
    <>
      <TopNav onChatClick={scrollToChat} />
      <div className="container">
        {/* Connection Status */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '16px' }}>
          <ConnectionStatus />
        </div>

        <Header />
        <div className="content-grid">
          <div className="main-content">
            <EventList />
          </div>
          <div className="sidebar">
            <Calendar />
            <ActionButtons scrollToChat={scrollToChat} />
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
    </>
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
