# Task 17: App.jsx Integration

## Batch: 3 (zadanie 1/2)

## Cel
Integracja OnboardingProvider i SurveyModal z głównym komponentem aplikacji.

## Plik
`src/App.jsx` (modyfikacja)

## Wymagania

1. **Importy:**
   - `OnboardingProvider`, `useOnboarding` z kontekstu
   - `SurveyModal` komponent

2. **Struktura providerów:**
   ```jsx
   <WebSocketProvider>
     <OnboardingProvider>
       <AppContent />
     </OnboardingProvider>
   </WebSocketProvider>
   ```

3. **Loading state:**
   - Gdy `isChecking` = true → pokaż spinner
   - Po sprawdzeniu → pokaż dashboard lub modal

4. **SurveyModal:**
   - Renderowany zawsze (sam kontroluje widoczność)
   - Pozycja: przed `ConnectionSnackbar`

## Zmiany w kodzie

```jsx
// Nowe importy
import { OnboardingProvider, useOnboarding } from './context/OnboardingContext';
import SurveyModal from './components/SurveyModal';

function AppContent() {
  // ... existing code ...
  const { isChecking } = useOnboarding();

  // Loading state
  if (isChecking) {
    return (
      <div className="app-loading">
        <div className="loading-spinner"></div>
        <p>Ładowanie...</p>
      </div>
    );
  }

  return (
    <div className="app-layout">
      {/* ... existing content ... */}

      {/* Survey Modal */}
      <SurveyModal />

      {/* Connection Snackbar */}
      <ConnectionSnackbar />
    </div>
  );
}

function App() {
  return (
    <WebSocketProvider>
      <OnboardingProvider>
        <AppContent />
      </OnboardingProvider>
    </WebSocketProvider>
  );
}
```

## Testy manualne

- [ ] Spinner widoczny podczas sprawdzania statusu
- [ ] Brak faktów → modal otwiera się automatycznie
- [ ] Ma fakty → dashboard bez modalu
- [ ] Modal można zamknąć i otworzyć ponownie
- [ ] WebSocket działa poprawnie w modalu

## Zależności
- Task 14: `OnboardingContext`
- Task 15: `SurveyModal`

## Następne zadanie
Task 18: Loading State CSS & Testing
