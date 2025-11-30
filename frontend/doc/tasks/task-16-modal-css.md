# Task 16: Modal CSS Styles

## Batch: 2 (zadanie 2/2)

## Cel
Dodanie stylów CSS dla modalu zgodnych z Material Design 3.

## Plik
`src/index.css` (modyfikacja - dodanie sekcji)

## Wymagania

1. **Overlay:**
   - Pełnoekranowe tło z rgba + blur
   - Animacja fadeIn

2. **Container:**
   - Białe tło, zaokrąglone rogi (extra-large)
   - Elevation level 3
   - Max-width 600-700px, max-height 90vh
   - Animacja slideUp

3. **Header:**
   - Flexbox: tytuł + przycisk zamknięcia
   - Border-bottom

4. **Body:**
   - Opis tekstowy
   - Container dla chatu z border

5. **Responsywność:**
   - Padding na mobile

## Style do dodania

```css
/* Modal Styles (M3) */

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 24px;
  animation: fadeIn var(--md-sys-motion-duration-medium) var(--md-sys-motion-easing-emphasized);
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-container {
  background: var(--md-sys-color-surface);
  border-radius: var(--md-sys-shape-corner-extra-large);
  box-shadow: var(--md-sys-elevation-3);
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: slideUp var(--md-sys-motion-duration-medium) var(--md-sys-motion-easing-emphasized-decelerate);
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(24px); }
  to { opacity: 1; transform: translateY(0); }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 24px 16px;
  border-bottom: 1px solid var(--md-sys-color-outline-variant);
}

.modal-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 500;
  color: var(--md-sys-color-on-surface);
}

.modal-close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: none;
  background: transparent;
  border-radius: var(--md-sys-shape-corner-full);
  cursor: pointer;
  color: var(--md-sys-color-on-surface-variant);
  transition: background var(--md-sys-motion-duration-short);
}

.modal-close-btn:hover {
  background: var(--md-sys-color-surface-variant);
}

.modal-body {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: 0 24px 24px;
}

.modal-description {
  margin: 16px 0;
  color: var(--md-sys-color-on-surface-variant);
  font-size: 14px;
}

.modal-chat-container {
  flex: 1;
  min-height: 400px;
  max-height: 60vh;
  overflow: hidden;
  border-radius: var(--md-sys-shape-corner-large);
  border: 1px solid var(--md-sys-color-outline-variant);
}

.modal-chat-container .chat-container {
  height: 100%;
  border-radius: 0;
  border: none;
}

.survey-modal {
  max-width: 700px;
}
```

## Testy manualne

- [ ] Animacje działają płynnie (fadeIn, slideUp)
- [ ] Blur na overlay
- [ ] Zaokrąglone rogi modalu
- [ ] Chat mieści się w modalu bez overflow
- [ ] Responsywność na mobile

## Zależności
- Istniejące design tokens w `index.css`

## Następne zadanie
Task 17: App.jsx Integration
