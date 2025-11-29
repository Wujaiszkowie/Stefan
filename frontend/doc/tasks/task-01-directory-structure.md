# Task 01: Create Directory Structure

## Phase
Phase 1: Infrastructure Setup

## Priority
High - Must be completed first

## Description
Create the foundational directory structure for the WebSocket integration. This establishes clear separation of concerns following feature-based architecture principles.

## Acceptance Criteria
- [ ] `src/services/` directory exists
- [ ] `src/hooks/` directory exists
- [ ] `src/context/` directory exists
- [ ] Each directory has an `index.js` barrel export file (optional, for cleaner imports)

## Implementation

### Commands
```bash
mkdir -p src/services src/hooks src/context
```

### Optional: Barrel Exports

Create `src/services/index.js`:
```javascript
export { default as wsService } from './websocket';
```

Create `src/hooks/index.js`:
```javascript
export { useWebSocket } from './useWebSocket';
```

Create `src/context/index.js`:
```javascript
export { WebSocketProvider, useWebSocketContext } from './WebSocketContext';
```

## Architecture Notes

### Why This Structure?

Following **co-location principle** from clean architecture:

- `services/` - Infrastructure layer, framework-agnostic code
- `hooks/` - React-specific abstractions over services
- `context/` - React state management, provider components

This separation allows:
1. Testing services without React
2. Swapping WebSocket implementation without touching components
3. Clear dependency direction: Components → Hooks → Context → Services

### Alternative Considered

Feature-based structure (`src/features/websocket/`) was considered but rejected:
- WebSocket is cross-cutting infrastructure, not a feature
- Used by multiple features (chat, survey, intervention)
- Service layer pattern is more appropriate here

## Dependencies
None

## Estimated Effort
5 minutes

## Files to Create
- `src/services/` (directory)
- `src/hooks/` (directory)
- `src/context/` (directory)
