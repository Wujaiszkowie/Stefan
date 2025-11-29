import React from 'react';

const EventCard = ({ title, time, badgeText, badgeColor, isActive }) => {
    const borderLeft = isActive ? '4px solid var(--accent-blue)' : 'none';
    const bg = isActive ? '#EFF6FF' : 'white'; // Light blue bg for active

    return (
        <div className="card" style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            borderLeft: borderLeft,
            backgroundColor: bg,
            padding: '16px'
        }}>
            <div>
                <h3 style={{ fontSize: '16px', marginBottom: '4px' }}>{title}</h3>
                <p style={{ fontSize: '14px' }}>{time}</p>
            </div>
            <span className={`badge ${badgeColor}`}>{badgeText}</span>
        </div>
    );
};

const EventList = () => {
    return (
        <div style={{ marginBottom: '32px' }}>
            <h2 style={{ fontSize: '18px', marginBottom: '16px' }}>Today's Events</h2>

            <EventCard
                title="Blood Pressure Check"
                time="10:00 AM - 10:15 AM"
                badgeText="Now"
                badgeColor="blue"
                isActive={true}
            />

            <EventCard
                title="Medication: Lisinopril"
                time="12:00 PM - With lunch"
                badgeText="In 2h"
                badgeColor="orange"
                isActive={false}
            />

            <EventCard
                title="Video Call with Dr. Chen"
                time="3:30 PM - 4:00 PM"
                badgeText="Later"
                badgeColor="gray"
                isActive={false}
            />
        </div>
    );
};

export default EventList;
