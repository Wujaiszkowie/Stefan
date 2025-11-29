# Task 07: Modify App.jsx with WebSocket Provider

## Phase
Phase 3: Component Integration

## Priority
High - Required for all WebSocket functionality

## Description
Wrap the application with `WebSocketProvider` and add the `ConnectionStatus` component to the layout. This enables WebSocket functionality across all child components.

## Acceptance Criteria
- [ ] App wrapped with `WebSocketProvider`
- [ ] `ConnectionStatus` component added to visible location
- [ ] Existing functionality preserved
- [ ] No visual regressions

## Current State

```javascript
// src/App.jsx (current)
import React, { useRef } from 'react';
import Header from './components/Header';
import EventList from './components/EventList';
import Calendar from './components/Calendar';
import ActionButtons from './components/ActionButtons';
import TopNav from './components/TopNav';
import Chat from './components/Chat';

function App() {
  const chatRef = useRef(null);

  const scrollToChat = () => {
    chatRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  return (
    <>
      <TopNav onChatClick={scrollToChat} />
      <div className="container">
        {/* ... */}
      </div>
    </>
  );
}

export default App;
```

## Implementation

### File: `src/App.jsx` (modified)

```javascript
import React, { useRef } from 'react';
import { WebSocketProvider } from './context/WebSocketContext';
import Header from './components/Header';
import EventList from './components/EventList';
import Calendar from './components/Calendar';
import ActionButtons from './components/ActionButtons';
import TopNav from './components/TopNav';
import Chat from './components/Chat';
import ConnectionStatus from './components/ConnectionStatus';

function App() {
  const chatRef = useRef(null);

  const scrollToChat = () => {
    chatRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  return (
    <WebSocketProvider>
      <TopNav onChatClick={scrollToChat} />
      <div className="container">
        {/* Connection Status - placed at top of main content */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '16px' }}>
          <ConnectionStatus />
        </div>

        <Header />
        <div className="content-grid">
          <div className="main-content">
            <EventList />
          </div>
          <div className="sidebar">
            <Calendar />
            <ActionButtons scrollToChat={scrollToChat} />
          </div>
        </div>

        {/* Chat Section */}
        <div ref={chatRef} style={{ marginTop: '48px' }}>
          <h2 style={{ fontSize: '24px', marginBottom: '24px' }}>Asystent AI</h2>
          <Chat />
        </div>
      </div>
    </WebSocketProvider>
  );
}

export default App;
```

## Key Changes

| Change | Reason |
|--------|--------|
| Import `WebSocketProvider` | Required for context |
| Import `ConnectionStatus` | Visual feedback |
| Wrap with `<WebSocketProvider>` | Enables context for all children |
| Add `<ConnectionStatus />` | User sees connection state |
| Pass `scrollToChat` to `ActionButtons` | Scroll to chat when action button clicked |

## Architecture Notes

### Provider Placement

The `WebSocketProvider` wraps the entire app at the top level because:
1. All components need access to WebSocket state
2. Single connection initialization point
3. Connection lifecycle tied to app lifecycle

### ConnectionStatus Placement

Two options were considered:

**Option A: In TopNav** (alternative)
```javascript
// TopNav.jsx
<nav>
  <ConnectionStatus />
</nav>
```

**Option B: Above content** (chosen)
- Doesn't require modifying TopNav
- Clear visibility
- Easy to remove/move later

### scrollToChat Prop

Passing `scrollToChat` to `ActionButtons` allows:
- User clicks "Emergency" → starts conversation + scrolls to chat
- Better UX for action button flow

## Alternative: ConnectionStatus in TopNav

If preferred, modify `TopNav.jsx` instead:

```javascript
// src/components/TopNav.jsx
import ConnectionStatus from './ConnectionStatus';

function TopNav({ onChatClick }) {
  return (
    <nav className="top-nav">
      <div className="nav-brand">Stefan</div>
      <div className="nav-actions" style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
        <ConnectionStatus />
        <button onClick={onChatClick}>Chat</button>
      </div>
    </nav>
  );
}
```

## Testing Checklist

- [ ] App renders without errors
- [ ] ConnectionStatus shows correct initial state
- [ ] All existing components render correctly
- [ ] Navigation still works
- [ ] Chat section scrolling works
- [ ] No console errors on mount

## Dependencies
- Task 05 (WebSocket context)
- Task 06 (ConnectionStatus component)

## Estimated Effort
10-15 minutes

## Files to Modify
- `src/App.jsx`

## Optional Files to Modify
- `src/components/TopNav.jsx` (if placing ConnectionStatus there)
