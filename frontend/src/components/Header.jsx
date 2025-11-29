import React from 'react';
import { useWebSocketContext } from '../context/WebSocketContext';

const Header = ({ scrollToChat }) => {
    const { isConnected, conversationType, startConversation } = useWebSocketContext();

    const today = new Date();
    const hour = today.getHours();

    // Polish greeting based on time of day
    let greeting = 'Dzien dobry';
    if (hour < 6) greeting = 'Dobrej nocy';
    else if (hour < 12) greeting = 'Dzien dobry';
    else if (hour < 18) greeting = 'Dzien dobry';
    else greeting = 'Dobry wieczor';

    const formattedDate = today.toLocaleDateString('pl-PL', {
        weekday: 'long',
        day: 'numeric',
        month: 'long'
    });

    const handleAction = (type) => {
        if (!isConnected) return;
        const success = startConversation(type);
        if (success && scrollToChat) {
            setTimeout(() => scrollToChat(), 100);
        }
    };

    const isEmergencyActive = conversationType === 'intervention';
    const isSupportActive = conversationType === 'support';

    return (
        <header style={{ marginBottom: '32px' }}>
            <div className="flex-row" style={{ alignItems: 'flex-start' }}>
                <div>
                    <h1 style={{
                        fontSize: '28px',
                        marginBottom: '4px',
                        fontWeight: '600',
                        color: 'var(--md-sys-color-on-surface)'
                    }}>
                        {greeting}, Marta
                    </h1>
                    <p style={{
                        fontSize: '14px',
                        color: 'var(--md-sys-color-on-surface-variant)',
                        textTransform: 'capitalize'
                    }}>
                        {formattedDate}
                    </p>
                </div>

                {/* Action Buttons + Bell */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    {/* Emergency Button */}
                    <button
                        className={`header-action-btn header-action-emergency ${isEmergencyActive ? 'active' : ''}`}
                        onClick={() => handleAction('intervention')}
                        disabled={!isConnected}
                        aria-label="Pomoc w nagłych wypadkach"
                        aria-pressed={isEmergencyActive}
                        title={!isConnected ? 'Brak połączenia' : 'Pomoc w nagłych wypadkach'}
                    >
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
                            <line x1="12" y1="9" x2="12" y2="13" />
                            <line x1="12" y1="17" x2="12.01" y2="17" />
                        </svg>
                        <span>Pomoc</span>
                    </button>

                    {/* Support/Chat Button */}
                    <button
                        className={`header-action-btn header-action-support ${isSupportActive ? 'active' : ''}`}
                        onClick={() => handleAction('support')}
                        disabled={!isConnected}
                        aria-label="Wsparcie psychiczne"
                        aria-pressed={isSupportActive}
                        title={!isConnected ? 'Brak połączenia' : 'Wsparcie psychiczne'}
                    >
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
                        </svg>
                        <span>Rozmowa</span>
                    </button>

                    {/* Bell Icon */}
                    <button
                        className="header-bell-btn"
                        aria-label="Powiadomienia (3 nowe)"
                    >
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--md-sys-color-primary)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
                            <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
                        </svg>
                        <span className="header-bell-badge">3</span>
                    </button>
                </div>
            </div>
        </header>
    );
};

export default Header;
