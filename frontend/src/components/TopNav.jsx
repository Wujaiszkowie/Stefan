import React from 'react';

const NavItem = ({ icon, label, isActive, onClick }) => {
    const color = isActive ? '#4B6EF6' : '#6B7280';

    return (
        <div
            onClick={onClick}
            style={{
                display: 'flex',
                alignItems: 'center',
                cursor: 'pointer',
                color: color,
                padding: '8px 16px',
                borderRadius: '8px',
                backgroundColor: isActive ? '#EFF6FF' : 'transparent',
                transition: 'all 0.2s'
            }}
        >
            {icon(color)}
            <span style={{ fontSize: '14px', marginLeft: '8px', fontWeight: '600' }}>{label}</span>
        </div>
    );
};

const TopNav = ({ onChatClick }) => {
    return (
        <div className="top-nav" style={{
            backgroundColor: 'white',
            borderBottom: '1px solid #E5E7EB',
            padding: '16px 32px',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: '32px'
        }}>
            <div style={{ fontSize: '20px', fontWeight: 'bold', color: '#1A1C1E' }}>ElderCare</div>

            <div style={{ display: 'flex', gap: '16px' }}>
                <NavItem
                    label="Home"
                    isActive={true}
                    onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
                    icon={(color) => (
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                            <polyline points="9 22 9 12 15 12 15 22"></polyline>
                        </svg>
                    )}
                />

                <NavItem
                    label="Chat"
                    isActive={false}
                    onClick={onChatClick}
                    icon={(color) => (
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                        </svg>
                    )}
                />

                <NavItem
                    label="Calendar"
                    isActive={false}
                    onClick={() => { }}
                    icon={(color) => (
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                            <line x1="16" y1="2" x2="16" y2="6"></line>
                            <line x1="8" y1="2" x2="8" y2="6"></line>
                            <line x1="3" y1="10" x2="21" y2="10"></line>
                        </svg>
                    )}
                />

                <NavItem
                    label="Settings"
                    isActive={false}
                    onClick={() => { }}
                    icon={(color) => (
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <circle cx="12" cy="12" r="3"></circle>
                            <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
                        </svg>
                    )}
                />
            </div>
        </div>
    );
};

export default TopNav;
