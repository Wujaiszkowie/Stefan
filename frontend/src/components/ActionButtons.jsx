import React from 'react';

const ActionButtons = () => {
    return (
        <div className="action-buttons">
            <button className="btn-large btn-emergency">
                <span>Emergency</span>
                <span>Help</span>
            </button>
            <button className="btn-large btn-mental">
                <span>I need mental</span>
                <span>support</span>
            </button>
        </div>
    );
};

export default ActionButtons;
