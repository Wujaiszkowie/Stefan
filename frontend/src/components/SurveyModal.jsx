/**
 * Survey Modal Component
 *
 * Modal dialog for onboarding survey.
 * Contains embedded Chat component configured for survey mode.
 * Auto-starts survey conversation when opened.
 */

import React, { useEffect } from 'react';
import { useWebSocketContext } from '../context/WebSocketContext';
import { useOnboarding } from '../context/OnboardingContext';
import Chat from './Chat';

const SurveyModal = () => {
  const { showSurveyModal, closeSurveyModal, markAsOnboarded } = useOnboarding();
  const {
    startConversation,
    endConversation,
    subscribe,
    isConnected,
    conversationType
  } = useWebSocketContext();

  // Handle modal close - end survey conversation
  const handleClose = () => {
    if (conversationType === 'survey') {
      endConversation();
    }
    closeSurveyModal();
  };

  // Auto-start survey when modal opens and WebSocket is connected
  useEffect(() => {
    if (showSurveyModal && isConnected && conversationType !== 'survey') {
      console.log('[SurveyModal] Auto-starting survey conversation');
      startConversation('survey');
    }
  }, [showSurveyModal, isConnected, conversationType, startConversation]);

  // Listen for survey completion to close modal
  useEffect(() => {
    if (!showSurveyModal) return;

    const unsubscribe = subscribe('survey_completed', (msg) => {
      console.log('[SurveyModal] Survey completed', msg.payload);
      markAsOnboarded();
    });

    return () => unsubscribe();
  }, [showSurveyModal, subscribe, markAsOnboarded]);

  // Handle Escape key and body scroll lock
  useEffect(() => {
    const handleEscapeKey = (e) => {
      if (e.key === 'Escape') {
        handleClose();
      }
    };

    if (showSurveyModal) {
      document.addEventListener('keydown', handleEscapeKey);
      document.body.style.overflow = 'hidden';
    }

    return () => {
      document.removeEventListener('keydown', handleEscapeKey);
      document.body.style.overflow = '';
    };
  }, [showSurveyModal, conversationType]);

  if (!showSurveyModal) return null;

  return (
    <div className="modal-overlay" onClick={handleClose}>
      <div
        className="modal-container survey-modal"
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="survey-modal-title"
      >
        <div className="modal-header">
          <h2 id="survey-modal-title">Uzupełnij profil podopiecznego</h2>
          <button
            className="modal-close-btn"
            onClick={handleClose}
            aria-label="Zamknij"
            title="Zamknij"
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <div className="modal-body">
          <p className="modal-description">
            Odpowiedz na kilka pytań, abyśmy mogli lepiej poznać potrzeby podopiecznego.
          </p>
          <div className="modal-chat-container">
            <Chat />
          </div>
        </div>
      </div>
    </div>
  );
};

export default SurveyModal;
