import React from 'react';

const Header = () => {
    const today = new Date();
    const formattedDate = today.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' });

    return (
        <div style={{ marginBottom: '24px' }}>
            <div className="flex-row" style={{ alignItems: 'flex-start' }}>
                <div>
                    <h1 style={{ fontSize: '24px', marginBottom: '4px' }}>Good morning, Martha</h1>
                    <p style={{ fontSize: '14px' }}>{formattedDate}</p>
                </div>
                <div style={{ position: 'relative', cursor: 'pointer' }}>
                    {/* Bell Icon */}
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#4B6EF6" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
                        <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
                    </svg>
                    {/* Notification Badge */}
                    <div style={{
                        position: 'absolute',
                        top: '-5px',
                        right: '-5px',
                        backgroundColor: '#EF4444',
                        color: 'white',
                        borderRadius: '50%',
                        width: '16px',
                        height: '16px',
                        fontSize: '10px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontWeight: 'bold'
                    }}>
                        3
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Header;
