# Task 14: Onboarding Context

## Batch: 1 (zadanie 2/2)

## Cel
Utworzenie kontekstu React do zarządzania stanem onboardingu - sprawdzanie czy użytkownik ma fakty i sterowanie modalem Survey.

## Plik
`src/context/OnboardingContext.jsx`

## Wymagania

1. **Stan:**
   - `isOnboarded` - `null` (sprawdzanie) | `true` | `false`
   - `isChecking` - czy trwa sprawdzanie
   - `showSurveyModal` - czy modal jest otwarty

2. **Efekty:**
   - Na mount: sprawdź status onboardingu via `checkOnboardingStatus()`
   - Jeśli `!isOnboarded` → automatycznie otwórz modal

3. **Metody:**
   - `openSurveyModal()` - otwiera modal
   - `closeSurveyModal()` - zamyka modal
   - `markAsOnboarded()` - ustawia `isOnboarded=true` i zamyka modal

4. **Hook:**
   - `useOnboarding()` - dostęp do kontekstu

## Implementacja

```javascript
import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { checkOnboardingStatus } from '../services/api';

const OnboardingContext = createContext(null);

export function OnboardingProvider({ children }) {
  const [isOnboarded, setIsOnboarded] = useState(null);
  const [isChecking, setIsChecking] = useState(true);
  const [showSurveyModal, setShowSurveyModal] = useState(false);

  useEffect(() => {
    async function checkStatus() {
      setIsChecking(true);
      const status = await checkOnboardingStatus();
      setIsOnboarded(status);
      setIsChecking(false);
      if (!status) {
        setShowSurveyModal(true);
      }
    }
    checkStatus();
  }, []);

  const openSurveyModal = useCallback(() => setShowSurveyModal(true), []);
  const closeSurveyModal = useCallback(() => setShowSurveyModal(false), []);
  const markAsOnboarded = useCallback(() => {
    setIsOnboarded(true);
    setShowSurveyModal(false);
  }, []);

  const value = {
    isOnboarded,
    isChecking,
    showSurveyModal,
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

export function useOnboarding() {
  const context = useContext(OnboardingContext);
  if (!context) {
    throw new Error('useOnboarding must be used within an OnboardingProvider');
  }
  return context;
}
```

## Testy manualne

- [ ] `isChecking` = true na początku
- [ ] Po sprawdzeniu: `isChecking` = false, `isOnboarded` = true/false
- [ ] Brak faktów → `showSurveyModal` = true automatycznie
- [ ] `markAsOnboarded()` → zamyka modal i ustawia `isOnboarded` = true

## Zależności
- Task 13: `src/services/api.js`

## Następne zadanie
Task 15: SurveyModal Component
