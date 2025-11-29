# UI Improvement Plan - Material Design 3

## Current State Analysis

| Aspect | Current | Issues |
|--------|---------|--------|
| Layout | 2-column grid (2fr + 1fr) | Chat appears below fold, requires scroll |
| Buttons | Full-width stacked | No visual hierarchy, all same size |
| Cards | 20px radius, light shadows | Good, but inconsistent elevation |
| Navigation | TopNav with text links | No active state persistence |
| Chat | Hidden until conversation starts | Good decision |

---

## Proposed Changes

### 1. Layout Restructure - Chat-Centric Design

```
Current:                          Proposed:
┌─────────────────────────┐      ┌─────────────────────────┐
│ TopNav                  │      │ TopNav (simplified)     │
├───────────────┬─────────┤      ├─────────┬───────────────┤
│ EventList     │Calendar │      │ Sidebar │ Main Area     │
│               │         │      │ • Events│ • Chat (full) │
│               │ Buttons │      │ • FABs  │ OR            │
├───────────────┴─────────┤      │         │ • Dashboard   │
│ Chat (below fold)       │      │         │               │
└─────────────────────────┘      └─────────┴───────────────┘
```

**Why**: Emergency app should have chat prominent, not hidden below scroll.

---

### 2. Action Buttons → FABs (Floating Action Buttons)

**Current**: Large full-width stacked buttons in sidebar

**Proposed**: M3 Extended FABs positioned bottom-right

```jsx
// Two FABs: Emergency (large, red) + Support (regular, blue)
<div className="fab-container">
  <ExtendedFAB
    icon={<AlertIcon />}
    label="Pomoc!"
    variant="emergency"  // Large, red, prominent
  />
  <FAB
    icon={<ChatIcon />}
    variant="support"    // Regular size, blue
  />
</div>
```

**Benefits**:
- Always visible (fixed position)
- Clear visual hierarchy (emergency larger)
- M3 compliant with elevation shadows

**CSS Changes**:
```css
.fab-container {
  position: fixed;
  bottom: 24px;
  right: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  z-index: 1000;
}

.fab {
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 3px 5px -1px rgba(0,0,0,0.2),
              0 6px 10px 0 rgba(0,0,0,0.14),
              0 1px 18px 0 rgba(0,0,0,0.12);
}

.fab-extended {
  padding: 16px 24px;
  border-radius: 16px;
  font-size: 1rem;
  font-weight: 500;
  gap: 12px;
}

.fab-regular {
  width: 56px;
  height: 56px;
  border-radius: 16px;
}

.fab-emergency {
  background-color: #E53935;
  color: white;
}

.fab-support {
  background-color: #4267B2;
  color: white;
}
```

---

### 3. Navigation → Navigation Rail

For a healthcare app targeting elderly users:

```
┌──────┬─────────────────────┐
│  🏠  │                     │
│ Home │                     │
│      │                     │
│  💬  │    Main Content     │
│ Chat │                     │
│      │                     │
│  📅  │                     │
│ Cal  │                     │
│      │                     │
│  ⚙️  │                     │
│ Set  │                     │
└──────┴─────────────────────┘
```

**Benefits**:
- Larger touch targets
- Always visible
- Better for elderly users
- Follows M3 guidelines for medium screens

**Component Structure**:
```jsx
const NavRail = () => (
  <nav className="nav-rail">
    <NavRailItem icon={<HomeIcon />} label="Strona główna" active />
    <NavRailItem icon={<ChatIcon />} label="Czat" />
    <NavRailItem icon={<CalendarIcon />} label="Kalendarz" />
    <NavRailItem icon={<SettingsIcon />} label="Ustawienia" />
  </nav>
);
```

**CSS**:
```css
.nav-rail {
  width: 80px;
  height: 100vh;
  background: var(--md-sys-color-surface);
  border-right: 1px solid var(--md-sys-color-outline-variant);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 0;
  gap: 4px;
  position: fixed;
  left: 0;
  top: 0;
}

.nav-rail-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 16px;
  border-radius: 16px;
  cursor: pointer;
  transition: background 0.2s;
  width: 56px;
}

.nav-rail-item:hover {
  background: var(--md-sys-color-surface-variant);
}

.nav-rail-item.active {
  background: var(--md-sys-color-secondary-container);
  color: var(--md-sys-color-on-secondary-container);
}

.nav-rail-item-label {
  font-size: 12px;
  margin-top: 4px;
  font-weight: 500;
}
```

---

### 4. Cards → M3 Elevated/Filled Variants

**Current**: All cards same style

**Proposed**: Different card types for different purposes

| Content | Card Type | Elevation |
|---------|-----------|-----------|
| Active event | Filled (tonal) | Level 0 |
| Upcoming events | Elevated | Level 1 |
| Calendar | Outlined | Level 0 |
| Chat messages | Filled | Level 0 |

**CSS**:
```css
.card-elevated {
  background: var(--md-sys-color-surface);
  box-shadow: var(--md-sys-elevation-1);
  border-radius: 12px;
}

.card-filled {
  background: var(--md-sys-color-surface-variant);
  border-radius: 12px;
}

.card-outlined {
  background: var(--md-sys-color-surface);
  border: 1px solid var(--md-sys-color-outline-variant);
  border-radius: 12px;
}
```

---

### 5. Chat Input → M3 Text Field

**Current**: Simple textarea with send button

**Proposed**: M3 outlined text field with integrated actions

```jsx
<div className="chat-input-m3">
  <TextField
    variant="outlined"
    placeholder="Wpisz wiadomość..."
    multiline
    trailingIcon={<SendIcon />}
    supportingText={isConnected ? null : "Brak połączenia"}
  />
</div>
```

**CSS**:
```css
.chat-input-m3 {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 12px 16px;
  background: var(--md-sys-color-surface);
  border-top: 1px solid var(--md-sys-color-outline-variant);
}

.chat-input-m3 textarea {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid var(--md-sys-color-outline);
  border-radius: 24px;
  font-family: inherit;
  font-size: 16px;
  resize: none;
  outline: none;
  transition: border-color 0.2s;
}

.chat-input-m3 textarea:focus {
  border-color: var(--md-sys-color-primary);
}
```

---

### 6. Connection Status → M3 Snackbar

**Current**: Small badge in corner

**Proposed**:
- Connected: Hidden (no distraction)
- Disconnected: Snackbar notification with retry action

```jsx
{!isConnected && (
  <Snackbar
    message="Brak połączenia z serwerem"
    action={<Button onClick={reconnect}>Ponów</Button>}
    variant="error"
  />
)}
```

**CSS**:
```css
.snackbar {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  background: var(--md-sys-color-inverse-surface);
  color: var(--md-sys-color-inverse-on-surface);
  padding: 14px 16px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: var(--md-sys-elevation-3);
  z-index: 1100;
}

.snackbar-error {
  background: var(--md-sys-color-error);
  color: var(--md-sys-color-on-error);
}

.snackbar-action {
  color: var(--md-sys-color-inverse-primary);
  font-weight: 500;
  cursor: pointer;
}
```

---

### 7. Color System → M3 Design Tokens

```css
:root {
  /* Primary */
  --md-sys-color-primary: #3B82F6;
  --md-sys-color-on-primary: #FFFFFF;
  --md-sys-color-primary-container: #D6E3FF;
  --md-sys-color-on-primary-container: #001A41;

  /* Secondary */
  --md-sys-color-secondary: #4267B2;
  --md-sys-color-on-secondary: #FFFFFF;
  --md-sys-color-secondary-container: #D6E3FF;
  --md-sys-color-on-secondary-container: #001A41;

  /* Error (for emergency) */
  --md-sys-color-error: #E53935;
  --md-sys-color-on-error: #FFFFFF;
  --md-sys-color-error-container: #FFEDEA;
  --md-sys-color-on-error-container: #410002;

  /* Surface */
  --md-sys-color-surface: #FFFFFF;
  --md-sys-color-surface-variant: #F3F4F6;
  --md-sys-color-on-surface: #111827;
  --md-sys-color-on-surface-variant: #6B7280;

  /* Outline */
  --md-sys-color-outline: #79747E;
  --md-sys-color-outline-variant: #E5E7EB;

  /* Inverse */
  --md-sys-color-inverse-surface: #313033;
  --md-sys-color-inverse-on-surface: #F4EFF4;
  --md-sys-color-inverse-primary: #D0BCFF;

  /* Shape */
  --md-sys-shape-corner-none: 0px;
  --md-sys-shape-corner-extra-small: 4px;
  --md-sys-shape-corner-small: 8px;
  --md-sys-shape-corner-medium: 12px;
  --md-sys-shape-corner-large: 16px;
  --md-sys-shape-corner-extra-large: 28px;
  --md-sys-shape-corner-full: 9999px;

  /* Elevation */
  --md-sys-elevation-0: none;
  --md-sys-elevation-1: 0 1px 3px 1px rgba(0,0,0,0.15), 0 1px 2px rgba(0,0,0,0.3);
  --md-sys-elevation-2: 0 2px 6px 2px rgba(0,0,0,0.15), 0 1px 2px rgba(0,0,0,0.3);
  --md-sys-elevation-3: 0 4px 8px 3px rgba(0,0,0,0.15), 0 1px 3px rgba(0,0,0,0.3);
}
```

---

### 8. Loading States → M3 Progress Indicators

**Current**: 3 bouncing dots

**Proposed**: M3 circular progress

```css
.circular-progress {
  width: 24px;
  height: 24px;
  border: 3px solid var(--md-sys-color-surface-variant);
  border-top-color: var(--md-sys-color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
```

---

## Implementation Priority

| Priority | Change | Impact | Effort |
|----------|--------|--------|--------|
| 1 | FABs for action buttons | High | Medium |
| 2 | Navigation Rail | High | Medium |
| 3 | Chat-centric layout | High | High |
| 4 | M3 color tokens | Medium | Low |
| 5 | Card elevation system | Medium | Low |
| 6 | Snackbar for connection | Medium | Low |
| 7 | M3 text field | Low | Medium |
| 8 | Loading indicators | Low | Low |

---

## Implementation Steps

### Phase 1: Foundation (M3 Design Tokens)
1. Add M3 CSS custom properties to index.css
2. Update existing components to use new tokens

### Phase 2: Navigation
1. Create NavRail component
2. Replace TopNav with NavRail
3. Update App layout to accommodate rail

### Phase 3: Action Buttons
1. Create FAB components
2. Replace ActionButtons with FABs
3. Position FABs fixed bottom-right

### Phase 4: Layout
1. Restructure App.jsx for chat-centric design
2. Move EventList and Calendar to sidebar
3. Make chat the main content area

### Phase 5: Polish
1. Update cards to M3 variants
2. Add Snackbar for connection status
3. Update loading indicators

---

## Resources

- [Material Design 3 Components](https://m3.material.io/components)
- [M3 Cards](https://m3.material.io/components/cards/overview)
- [M3 Buttons](https://m3.material.io/components/buttons/guidelines)
- [M3 Color System](https://m3.material.io/styles/color/overview)
