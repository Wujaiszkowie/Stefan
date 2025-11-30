# Wspiernik - Frontend

React-based frontend application for the Wspiernik caregiver support system.

## Tech Stack

- **React 18** - UI library
- **React Calendar** - Calendar component
- **WebSocket** - Real-time communication with backend
- **CSS** - Material Design 3 styling

## Project Structure

```
src/
├── components/
│   ├── App.jsx              # Main application component
│   ├── Header.jsx           # Header with greeting and action buttons
│   ├── NavRail.jsx          # M3 Navigation Rail
│   ├── Chat.jsx             # Real-time chat interface
│   ├── SurveyModal.jsx      # Onboarding survey modal
│   ├── Calendar.jsx         # Calendar widget
│   ├── EventList.jsx        # Events/tasks list
│   ├── ConnectionStatus.jsx # WebSocket connection indicator
│   └── Snackbar.jsx         # M3 Snackbar notifications
├── context/
│   ├── WebSocketContext.jsx # WebSocket state management
│   └── OnboardingContext.jsx # Onboarding flow state
├── services/
│   ├── websocket.js         # WebSocket service
│   └── api.js               # REST API client
├── hooks/
│   └── useWebSocket.js      # WebSocket hook
├── index.jsx                # Entry point
└── index.css                # Global styles (M3 tokens)
```

## Features

- **Real-time Chat** - WebSocket-based communication
- **Multiple Conversation Types**:
  - `intervention` - Emergency help (Pomoc w nagłych wypadkach)
  - `support` - Mental support (Wsparcie psychiczne)
  - `survey` - Profile completion (Uzupełnianie profilu)
- **Onboarding Flow** - Auto-detect if user needs to complete survey
- **Material Design 3** - Modern UI with M3 color system

## Getting Started

### Prerequisites

- Node.js 18+
- npm

### Installation

```bash
npm install
```

### Development

```bash
npm start
```

Opens http://localhost:3000

### Production Build

```bash
npm run build
```

## Configuration

Environment variables (optional):

| Variable | Default | Description |
|----------|---------|-------------|
| `REACT_APP_API_URL` | `http://localhost:8080` | Backend API URL |
| `REACT_APP_WS_URL` | `ws://localhost:8080/ws` | WebSocket URL |

## WebSocket Messages

### Outgoing (Frontend → Backend)

| Type | Payload | Description |
|------|---------|-------------|
| `survey_start` | `{}` | Start survey session |
| `survey_message` | `{ text }` | Send survey answer |
| `support_start` | `{}` | Start support session |
| `support_message` | `{ text }` | Send support message |
| `intervention_start` | `{}` | Start intervention |
| `intervention_message` | `{ text }` | Send intervention message |

### Incoming (Backend → Frontend)

| Type | Payload | Description |
|------|---------|-------------|
| `survey_question` | `{ question }` | Next survey question |
| `survey_completed` | `{ factsSavedCount }` | Survey finished |
| `support_message` | `{ text }` | Support response |
| `support_completed` | `{ duration }` | Support session ended |
| `intervention_question` | `{ question }` | Intervention question |
| `intervention_completed` | `{}` | Intervention finished |
| `error` | `{ message }` | Error message |

## REST API

| Method | Endpoint | Description |
|--------|----------|-------------|
| `HEAD` | `/api/fact` | Check if user has facts (onboarding status) |

Response:
- `200 OK` - User has facts (onboarded)
- `204 No Content` - No facts (needs onboarding)
