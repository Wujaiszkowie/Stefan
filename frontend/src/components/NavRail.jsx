/**
 * Navigation Rail Component (M3)
 *
 * Material Design 3 Navigation Rail for medium/large screens.
 * Provides larger touch targets suitable for elderly users.
 */

import React from 'react';

const NavRailItem = ({ icon, label, isActive, onClick }) => {
  return (
    <button
      className={`nav-rail-item ${isActive ? 'active' : ''}`}
      onClick={onClick}
      aria-current={isActive ? 'page' : undefined}
      aria-label={label}
    >
      <div className="nav-rail-icon">
        {icon}
      </div>
      <span className="nav-rail-label">{label}</span>
    </button>
  );
};

/**
 * @param {Object} props
 * @param {string} props.activeTab - Currently active tab id
 * @param {Function} props.onTabChange - Callback when tab changes
 * @param {Function} props.onChatClick - Callback for chat navigation
 */
const NavRail = ({ activeTab = 'home', onTabChange, onChatClick }) => {
  const handleTabClick = (tabId) => {
    if (onTabChange) {
      onTabChange(tabId);
    }
    if (tabId === 'chat' && onChatClick) {
      onChatClick();
    }
  };

  return (
    <nav className="nav-rail" aria-label="Nawigacja główna">
      {/* Logo/Brand at top */}
      <div className="nav-rail-brand">
        <svg
          width="32"
          height="32"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
        </svg>
      </div>

      {/* Navigation Items */}
      <div className="nav-rail-items">
        <NavRailItem
          label="Strona główna"
          isActive={activeTab === 'home'}
          onClick={() => handleTabClick('home')}
          icon={
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
              <polyline points="9 22 9 12 15 12 15 22" />
            </svg>
          }
        />

        <NavRailItem
          label="Czat"
          isActive={activeTab === 'chat'}
          onClick={() => handleTabClick('chat')}
          icon={
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
            </svg>
          }
        />

        <NavRailItem
          label="Kalendarz"
          isActive={activeTab === 'calendar'}
          onClick={() => handleTabClick('calendar')}
          icon={
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
              <line x1="16" y1="2" x2="16" y2="6" />
              <line x1="8" y1="2" x2="8" y2="6" />
              <line x1="3" y1="10" x2="21" y2="10" />
            </svg>
          }
        />

        <NavRailItem
          label="Ustawienia"
          isActive={activeTab === 'settings'}
          onClick={() => handleTabClick('settings')}
          icon={
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="12" cy="12" r="3" />
              <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" />
            </svg>
          }
        />
      </div>
    </nav>
  );
};

export default NavRail;
