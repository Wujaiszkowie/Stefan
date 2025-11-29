# Task 12: Final Cleanup and Testing

## Phase
Phase 4: Polish & Cleanup

## Priority
High - Quality assurance

## Description
Remove deprecated code, verify all flows work end-to-end, fix any issues discovered during integration, and ensure code quality.

## Acceptance Criteria
- [ ] Old HTTP fetch code removed from Chat.jsx
- [ ] No hardcoded URLs remain
- [ ] All three conversation flows work
- [ ] Reconnection behavior verified
- [ ] Error handling works correctly
- [ ] No console errors or warnings
- [ ] Code follows project conventions

## Cleanup Tasks

### 1. Remove HTTP Fetch Code

**File:** `src/components/Chat.jsx`

Remove any remaining fetch-related code:

```javascript
// REMOVE: Old fetch implementation
const response = await fetch('http://localhost:8000/api/v1/chat', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({ message: userMessage.content })
});
```

### 2. Remove Hardcoded URLs

Search for and remove/replace:

```bash
# Find hardcoded URLs
grep -r "localhost:8000" src/
grep -r "localhost:8080" src/
grep -r "http://" src/
grep -r "ws://" src/
```

All URLs should reference environment variables:
```javascript
// CORRECT
const WS_URL = process.env.REACT_APP_WS_URL || 'ws://localhost:8080/ws';

// INCORRECT (hardcoded)
const url = 'ws://localhost:8080/ws';
```

### 3. Remove Unused Imports

Check each modified file for unused imports:

```javascript
// Example: if useState is no longer used after refactor
import { useState, useRef, useEffect } from 'react'; // Check if all are used
```

### 4. Console.log Cleanup

Review and categorize console statements:

```javascript
// KEEP: Development debugging (wrap in condition)
if (process.env.NODE_ENV === 'development') {
  console.log('[WS] Debug info:', data);
}

// REMOVE: Temporary debugging
console.log('test');
console.log('here');

// KEEP: Meaningful logging
console.error('[WS] Connection failed:', error);
console.warn('[WS] Queue full, dropping message');
```

## Testing Checklist

### Connection Tests

- [ ] **Initial connection**: App connects to WebSocket on load
- [ ] **Connection indicator**: Shows "Połączono" when connected
- [ ] **Disconnect handling**: Shows "Rozłączono" when server stops
- [ ] **Auto-reconnect**: Reconnects after brief disconnection
- [ ] **Reconnect indicator**: Shows "Ponowne łączenie..." during attempts
- [ ] **Max attempts**: Stops after 5 failed reconnection attempts

### Survey Flow Tests

- [ ] Click "Uzupełnij profil" button
- [ ] Chat clears and shows loading
- [ ] First question received from backend
- [ ] User can type and send response
- [ ] Subsequent questions received
- [ ] Survey completion message shown
- [ ] Completion summary displays fact count

### Intervention Flow Tests

- [ ] Click "Pomoc w nagłych wypadkach" button
- [ ] Scenario matched message shown
- [ ] Questions received from backend
- [ ] User can respond to questions
- [ ] Intervention completion message shown
- [ ] Can start new conversation after completion

### Support Flow Tests

- [ ] Click "Wsparcie psychiczne" button
- [ ] Initial message received
- [ ] Conversational flow works
- [ ] Multiple back-and-forth messages
- [ ] Session completion shows duration
- [ ] Chat ready for new conversation

### Error Handling Tests

- [ ] Backend error message displays in Polish
- [ ] Error message styled differently (red)
- [ ] Sending while disconnected shows error/queues message
- [ ] Invalid JSON from backend handled gracefully

### UI/UX Tests

- [ ] Buttons disabled when disconnected
- [ ] Active conversation button highlighted
- [ ] Input placeholder changes based on state
- [ ] Send button disabled appropriately
- [ ] Scroll to bottom on new message
- [ ] Scroll to chat on action button click
- [ ] Loading dots animation works

### Accessibility Tests

- [ ] Keyboard navigation works (Tab, Enter)
- [ ] Screen reader announces connection status
- [ ] ARIA labels present on interactive elements
- [ ] Focus management after dialog close

## Code Quality Checklist

### File: `src/services/websocket.js`
- [ ] No direct DOM manipulation
- [ ] All methods documented with JSDoc
- [ ] Error handling in all async operations
- [ ] Singleton exported correctly

### File: `src/hooks/useWebSocket.js`
- [ ] Cleanup functions returned
- [ ] No memory leaks (subscriptions cleaned)
- [ ] Stable references with useCallback

### File: `src/context/WebSocketContext.jsx`
- [ ] Provider wraps children correctly
- [ ] Context error thrown when used outside provider
- [ ] All state updates handled correctly

### File: `src/components/Chat.jsx`
- [ ] No unused state variables
- [ ] All subscriptions cleaned up
- [ ] No direct service access (uses context)

### File: `src/components/ActionButtons.jsx`
- [ ] Buttons properly disabled
- [ ] Click handlers don't cause errors when disconnected

### File: `src/components/ConnectionStatus.jsx`
- [ ] All states handled
- [ ] Proper ARIA attributes

## Manual Test Script

```
1. Start backend: ./mvnw quarkus:dev
2. Start frontend: npm start
3. Open browser to http://localhost:3000
4. Verify connection indicator shows "Połączono"

5. SURVEY TEST:
   - Click "Uzupełnij profil"
   - Answer each question
   - Verify completion message

6. INTERVENTION TEST:
   - Click "Pomoc w nagłych wypadkach"
   - Follow the intervention flow
   - Verify completion

7. SUPPORT TEST:
   - Click "Wsparcie psychiczne"
   - Have a brief conversation
   - Verify responses

8. DISCONNECT TEST:
   - Stop backend (Ctrl+C)
   - Verify "Rozłączono" shown
   - Verify buttons disabled
   - Restart backend
   - Verify auto-reconnect

9. MID-CONVERSATION SWITCH:
   - Start support conversation
   - Click "Uzupełnij profil"
   - Verify confirmation dialog
   - Confirm switch
   - Verify new conversation starts
```

## Final File Structure

```
src/
├── services/
│   └── websocket.js          ✓ WebSocket service
├── hooks/
│   └── useWebSocket.js       ✓ React hook
├── context/
│   └── WebSocketContext.jsx  ✓ Context provider
├── components/
│   ├── Chat.jsx              ✓ Refactored
│   ├── ActionButtons.jsx     ✓ Refactored
│   ├── ConnectionStatus.jsx  ✓ New
│   ├── CompletionSummary.jsx ✓ New (optional)
│   └── [other existing]
├── App.jsx                   ✓ Modified
└── index.css                 ✓ Modified
```

## Environment Files

```
.env                          ✓ Created
.env.example                  ✓ Created
```

## Known Issues / Future Improvements

Document any discovered issues for future work:

1. **Issue**: [Description]
   - **Workaround**: [If any]
   - **Future fix**: [Suggested approach]

2. **Enhancement**: Message persistence across page refresh
   - **Status**: Not implemented
   - **Reason**: Out of scope for MVP

## Dependencies
- All previous tasks (1-11)

## Estimated Effort
60-90 minutes (includes testing time)

## Files to Review
- All files created/modified in tasks 1-11
