import React from 'react';

const EventCard = ({ title, time, badgeText, badgeColor, isActive, icon }) => {
    return (
        <div
            className="card"
            style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                borderLeft: isActive ? '4px solid var(--md-sys-color-primary)' : 'none',
                backgroundColor: isActive ? 'var(--md-sys-color-primary-container)' : 'var(--md-sys-color-surface)',
                padding: '16px 20px',
                boxShadow: isActive ? 'var(--md-sys-elevation-2)' : 'var(--md-sys-elevation-1)',
                borderRadius: 'var(--md-sys-shape-corner-medium)'
            }}
        >
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                {/* Icon */}
                <div style={{
                    width: '40px',
                    height: '40px',
                    borderRadius: 'var(--md-sys-shape-corner-full)',
                    backgroundColor: isActive ? 'var(--md-sys-color-primary)' : 'var(--md-sys-color-surface-variant)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: isActive ? 'var(--md-sys-color-on-primary)' : 'var(--md-sys-color-on-surface-variant)',
                    flexShrink: 0
                }}>
                    {icon}
                </div>
                <div>
                    <h3 style={{
                        fontSize: '16px',
                        marginBottom: '4px',
                        fontWeight: '500',
                        color: isActive ? 'var(--md-sys-color-on-primary-container)' : 'var(--md-sys-color-on-surface)'
                    }}>
                        {title}
                    </h3>
                    <p style={{
                        fontSize: '14px',
                        color: isActive ? 'var(--md-sys-color-on-primary-container)' : 'var(--md-sys-color-on-surface-variant)'
                    }}>
                        {time}
                    </p>
                </div>
            </div>
            <span className={`badge ${badgeColor}`}>{badgeText}</span>
        </div>
    );
};

const EventList = () => {
    // Icons
    const heartIcon = (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M20.42 4.58a5.4 5.4 0 0 0-7.65 0l-.77.78-.77-.78a5.4 5.4 0 0 0-7.65 7.65l.77.77L12 20.65l7.65-7.65.77-.77a5.4 5.4 0 0 0 0-7.65z"/>
        </svg>
    );

    const pillIcon = (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M10.5 20.5L3.5 13.5a4.95 4.95 0 1 1 7-7l7 7a4.95 4.95 0 1 1-7 7z"/>
            <path d="M8.5 8.5l7 7"/>
        </svg>
    );

    const videoIcon = (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <polygon points="23 7 16 12 23 17 23 7"/>
            <rect x="1" y="5" width="15" height="14" rx="2" ry="2"/>
        </svg>
    );

    return (
        <section style={{ marginBottom: '32px' }}>
            <h2 style={{
                fontSize: '18px',
                marginBottom: '16px',
                fontWeight: '600',
                color: 'var(--md-sys-color-on-surface)'
            }}>
                Dzisiejsze wydarzenia
            </h2>

            <EventCard
                title="Pomiar cisnienia"
                time="10:00 - 10:15"
                badgeText="Teraz"
                badgeColor="blue"
                isActive={true}
                icon={heartIcon}
            />

            <EventCard
                title="Lek: Lisinopril"
                time="12:00 - Do obiadu"
                badgeText="Za 2h"
                badgeColor="orange"
                isActive={false}
                icon={pillIcon}
            />

            <EventCard
                title="Rozmowa z dr. Kowalska"
                time="15:30 - 16:00"
                badgeText="Pozniej"
                badgeColor="gray"
                isActive={false}
                icon={videoIcon}
            />
        </section>
    );
};

export default EventList;
