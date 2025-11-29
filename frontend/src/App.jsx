import React from 'react';
import Header from './components/Header';
import EventList from './components/EventList';
import Calendar from './components/Calendar';
import ActionButtons from './components/ActionButtons';
import TopNav from './components/TopNav';

function App() {
  return (
    <>
      <TopNav />
      <div className="container">
        <Header />
        <div className="content-grid">
          <div className="main-content">
            <EventList />
          </div>
          <div className="sidebar">
            <Calendar />
            <ActionButtons />
          </div>
        </div>
      </div>
    </>
  );
}

export default App;
