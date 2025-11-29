import React, { useState } from 'react';
import Calendar from 'react-calendar';
import 'react-calendar/dist/Calendar.css';

const CalendarComponent = () => {
    const [value, onChange] = useState(new Date());
    const [activeStartDate, setActiveStartDate] = useState(new Date());

    // Dynamic events relative to current month
    const currentYear = activeStartDate.getFullYear();
    const currentMonth = activeStartDate.getMonth();

    const events = [
        new Date(currentYear, currentMonth, 14).toDateString(),
        new Date(currentYear, currentMonth, 16).toDateString(),
        new Date(currentYear, currentMonth, 21).toDateString(),
    ];

    const tileContent = ({ date, view }) => {
        if (view === 'month' && events.includes(date.toDateString())) {
            return <div className="calendar-dot"></div>;
        }
        return null;
    };

    const handleActiveStartDateChange = ({ activeStartDate }) => {
        setActiveStartDate(activeStartDate);
    };

    const formattedMonthYear = activeStartDate.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });

    return (
        <div style={{ marginBottom: '32px' }}>
            <h2 style={{ fontSize: '18px', marginBottom: '16px' }}>{formattedMonthYear}</h2>
            <div className="card calendar-wrapper" style={{ padding: '16px', border: 'none' }}>
                <Calendar
                    onChange={onChange}
                    value={value}
                    tileContent={tileContent}
                    prev2Label={null}
                    next2Label={null}
                    showNeighboringMonth={false}
                    onActiveStartDateChange={handleActiveStartDateChange}
                    formatShortWeekday={(locale, date) => ['S', 'M', 'T', 'W', 'T', 'F', 'S'][date.getDay()]}
                />
            </div>
        </div>
    );
};

export default CalendarComponent;