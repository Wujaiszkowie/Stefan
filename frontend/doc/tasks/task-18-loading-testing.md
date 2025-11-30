# Task 18: Loading State CSS & Final Testing

## Batch: 3 (zadanie 2/2)

## Cel
Dodanie stylów dla stanu ładowania oraz przeprowadzenie końcowych testów integracyjnych.

## Plik
`src/index.css` (modyfikacja - dodanie sekcji)

## Wymagania

1. **Loading spinner:**
   - Wycentrowany na ekranie
   - Animowany spinner (rotate)
   - Tekst "Ładowanie..."

2. **Testy integracyjne:**
   - Pełny flow onboardingu
   - Edge cases

## Style do dodania

```css
/* App Loading State */

.app-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  gap: 16px;
  color: var(--md-sys-color-on-surface-variant);
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid var(--md-sys-color-surface-variant);
  border-top-color: var(--md-sys-color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
```

## Checklist testów

### Scenariusz 1: Nowy użytkownik (brak faktów)
- [ ] Start aplikacji
- [ ] Widoczny spinner "Ładowanie..."
- [ ] Modal Survey otwiera się automatycznie
- [ ] Chat startuje z pytaniem survey
- [ ] Odpowiedzi są wysyłane przez WebSocket
- [ ] Po `survey_completed` modal się zamyka
- [ ] Dashboard jest widoczny

### Scenariusz 2: Powracający użytkownik (ma fakty)
- [ ] Start aplikacji
- [ ] Widoczny spinner "Ładowanie..."
- [ ] Dashboard wyświetla się bez modalu
- [ ] Można używać innych funkcji (Pomoc, Rozmowa)

### Scenariusz 3: Zamknięcie bez ukończenia
- [ ] Otwórz modal
- [ ] Zamknij przez X / Escape / overlay click
- [ ] Dashboard widoczny
- [ ] Przy następnym refresh → modal pojawia się ponownie

### Scenariusz 4: Błąd połączenia
- [ ] Wyłącz backend
- [ ] Start aplikacji
- [ ] Dashboard wyświetla się (fail-safe)
- [ ] Komunikat o braku połączenia

### Scenariusz 5: Accessibility
- [ ] Tab navigation działa w modalu
- [ ] Escape zamyka modal
- [ ] Screen reader czyta tytuł modalu

## Zależności
- Wszystkie poprzednie taski (13-17)

## Po zakończeniu
- Commit zmian
- Aktualizacja dokumentacji jeśli potrzebna
