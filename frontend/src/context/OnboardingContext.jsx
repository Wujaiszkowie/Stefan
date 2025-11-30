/**
 * Onboarding Context Provider
 *
 * Manages onboarding state - checks if user has completed initial survey.
 * Shows survey modal automatically if onboarding is not completed.
 */

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { checkOnboardingStatus } from '../services/api';

const OnboardingContext = createContext(null);

/**
 * Onboarding Provider Component
 */
export function OnboardingProvider({ children }) {
  // null = checking, true = onboarded, false = needs onboarding
  const [isOnboarded, setIsOnboarded] = useState(null);
  const [isChecking, setIsChecking] = useState(true);
  const [showSurveyModal, setShowSurveyModal] = useState(false);

  // Check onboarding status on mount
  useEffect(() => {
    async function checkStatus() {
      setIsChecking(true);

      const status = await checkOnboardingStatus();
      console.log('[OnboardingContext] Onboarding status:', status ? 'completed' : 'needed');

      setIsOnboarded(status);
      setIsChecking(false);

      // Auto-show modal if not onboarded
      if (!status) {
        setShowSurveyModal(true);
      }
    }

    checkStatus();
  }, []);

  /**
   * Open the survey modal manually
   */
  const openSurveyModal = useCallback(() => {
    setShowSurveyModal(true);
  }, []);

  /**
   * Close the survey modal
   */
  const closeSurveyModal = useCallback(() => {
    setShowSurveyModal(false);
  }, []);

  /**
   * Mark user as onboarded and close modal
   * Called when survey is completed
   */
  const markAsOnboarded = useCallback(() => {
    console.log('[OnboardingContext] Marking as onboarded');
    setIsOnboarded(true);
    setShowSurveyModal(false);
  }, []);

  const value = {
    // State
    isOnboarded,
    isChecking,
    showSurveyModal,

    // Methods
    openSurveyModal,
    closeSurveyModal,
    markAsOnboarded,
  };

  return (
    <OnboardingContext.Provider value={value}>
      {children}
    </OnboardingContext.Provider>
  );
}

/**
 * Hook to access Onboarding context
 * @returns {Object} Onboarding context value
 * @throws {Error} If used outside of OnboardingProvider
 */
export function useOnboarding() {
  const context = useContext(OnboardingContext);

  if (!context) {
    throw new Error('useOnboarding must be used within an OnboardingProvider');
  }

  return context;
}

export default OnboardingContext;
