# Task 02: Create Environment Configuration

## Phase
Phase 1: Infrastructure Setup

## Priority
High - Required before WebSocket service

## Description
Create environment configuration for WebSocket connection parameters. This externalizes configuration, enabling different settings for development, staging, and production environments.

## Acceptance Criteria
- [ ] `.env` file created with WebSocket configuration
- [ ] `.env.example` file created for documentation
- [ ] Configuration values accessible via `process.env`

## Implementation

### Create `.env`
```env
# WebSocket Configuration
REACT_APP_WS_URL=ws://localhost:8080/ws

# Reconnection Settings
REACT_APP_WS_RECONNECT_DELAY=3000
REACT_APP_WS_MAX_RECONNECT_ATTEMPTS=5
```

### Create `.env.example`
```env
# WebSocket Configuration
# URL for WebSocket connection to backend
REACT_APP_WS_URL=ws://localhost:8080/ws

# Reconnection Settings
# Base delay in milliseconds before reconnect attempt
REACT_APP_WS_RECONNECT_DELAY=3000
# Maximum number of reconnection attempts before giving up
REACT_APP_WS_MAX_RECONNECT_ATTEMPTS=5
```

### Update `.gitignore` (if not already present)
```gitignore
# Environment files
.env
.env.local
.env.*.local
```

## Usage in Code

```javascript
// In websocket.js service
const WS_URL = process.env.REACT_APP_WS_URL || 'ws://localhost:8080/ws';
const RECONNECT_DELAY = parseInt(process.env.REACT_APP_WS_RECONNECT_DELAY) || 3000;
const MAX_RECONNECT_ATTEMPTS = parseInt(process.env.REACT_APP_WS_MAX_RECONNECT_ATTEMPTS) || 5;
```

## Architecture Notes

### Why REACT_APP_ Prefix?

Create React App (react-scripts) only exposes environment variables prefixed with `REACT_APP_` to the browser bundle. This is a security feature preventing accidental exposure of server-side secrets.

### Default Values

All environment variables have sensible defaults in code. This ensures:
1. App works without `.env` file during development
2. CI/CD can override via environment without file changes
3. Fail-safe behavior if configuration is missing

### Docker Considerations

For Docker deployment, these can be overridden:
```yaml
# docker-compose.yml
services:
  frontend:
    environment:
      - REACT_APP_WS_URL=ws://backend:8080/ws
```

## Dependencies
- Task 01 (directory structure) - not strictly required but logical ordering

## Estimated Effort
5 minutes

## Files to Create
- `.env`
- `.env.example`

## Files to Modify
- `.gitignore` (if needed)
