# Task 06: Create Connection Status Component

## Phase
Phase 2: React Integration Layer

## Priority
Medium - UX enhancement

## Description
Create a visual indicator component showing the current WebSocket connection state. Provides user feedback about connectivity and aids debugging.

## Acceptance Criteria
- [ ] Displays connection state with colored indicator
- [ ] States: connected (green), disconnected (red), reconnecting (yellow)
- [ ] Polish language labels
- [ ] Compact design suitable for header placement
- [ ] Optional: shows reconnection attempt count
- [ ] Accessible (proper ARIA attributes)

## Implementation

### File: `src/components/ConnectionStatus.jsx`

```javascript
/**
 * Connection Status Indicator
 *
 * Visual indicator for WebSocket connection state.
 * Designed for placement in header/navigation.
 */

import React from 'react';
import { useWebSocketContext } from '../context/WebSocketContext';

const STATUS_CONFIG = {
  connected: {
    color: '#22c55e', // green-500
    bgColor: '#dcfce7', // green-100
    label: 'Połączono',
    ariaLabel: 'Połączenie aktywne'
  },
  disconnected: {
    color: '#ef4444', // red-500
    bgColor: '#fee2e2', // red-100
    label: 'Rozłączono',
    ariaLabel: 'Brak połączenia'
  },
  connecting: {
    color: '#eab308', // yellow-500
    bgColor: '#fef9c3', // yellow-100
    label: 'Łączenie...',
    ariaLabel: 'Nawiązywanie połączenia'
  },
  reconnecting: {
    color: '#f97316', // orange-500
    bgColor: '#ffedd5', // orange-100
    label: 'Ponowne łączenie...',
    ariaLabel: 'Ponowne nawiązywanie połączenia'
  }
};

/**
 * @param {Object} props
 * @param {boolean} [props.showLabel=true] - Whether to show text label
 * @param {string} [props.className] - Additional CSS classes
 */
function ConnectionStatus({ showLabel = true, className = '' }) {
  const { connectionState, isInitialized } = useWebSocketContext();

  // Don't render until initialized to prevent flicker
  if (!isInitialized) {
    return null;
  }

  const config = STATUS_CONFIG[connectionState] || STATUS_CONFIG.disconnected;

  const containerStyle = {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '6px',
    padding: '4px 10px',
    borderRadius: '9999px',
    backgroundColor: config.bgColor,
    fontSize: '12px',
    fontWeight: '500'
  };

  const dotStyle = {
    width: '8px',
    height: '8px',
    borderRadius: '50%',
    backgroundColor: config.color,
    animation: connectionState === 'reconnecting' ? 'pulse 1.5s infinite' : 'none'
  };

  const labelStyle = {
    color: config.color
  };

  return (
    <div
      className={`connection-status ${className}`}
      style={containerStyle}
      role="status"
      aria-label={config.ariaLabel}
      aria-live="polite"
    >
      <span style={dotStyle} aria-hidden="true" />
      {showLabel && (
        <span style={labelStyle}>{config.label}</span>
      )}
    </div>
  );
}

export default ConnectionStatus;
```

### CSS Addition (add to `src/index.css`)

```css
/* Connection Status Animation */
@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.connection-status {
  transition: background-color 0.3s ease, color 0.3s ease;
}
```

## Usage Example

### In TopNav
```javascript
import ConnectionStatus from './ConnectionStatus';

function TopNav() {
  return (
    <nav className="top-nav">
      <div className="nav-brand">Stefan</div>
      <div className="nav-actions">
        <ConnectionStatus />
        {/* other nav items */}
      </div>
    </nav>
  );
}
```

### Dot Only (compact)
```javascript
<ConnectionStatus showLabel={false} />
```

### Custom Styling
```javascript
<ConnectionStatus className="my-custom-status" />
```

## Architecture Notes

### Why Inline Styles?

The component uses inline styles with a CSS fallback for animation:
1. **Self-contained**: No external CSS dependencies
2. **Configurable**: Colors can be easily themed
3. **SSR-safe**: No CSS-in-JS hydration issues

For a more robust solution, consider migrating to Tailwind classes:
```javascript
const STATUS_CONFIG = {
  connected: {
    classes: 'bg-green-100 text-green-500',
    dotClass: 'bg-green-500',
    // ...
  }
};
```

### Accessibility

- `role="status"` identifies this as a status indicator
- `aria-label` provides context for screen readers
- `aria-live="polite"` announces changes without interrupting
- `aria-hidden="true"` on decorative dot

### Animation Consideration

Pulse animation on reconnecting state:
- Provides visual feedback that something is happening
- Uses CSS animation (not JS) for performance
- Respects `prefers-reduced-motion` (optional enhancement):

```css
@media (prefers-reduced-motion: reduce) {
  .connection-status span {
    animation: none !important;
  }
}
```

## Variants

### Compact Version
```javascript
function ConnectionDot() {
  const { connectionState } = useWebSocketContext();
  const config = STATUS_CONFIG[connectionState];

  return (
    <span
      style={{
        width: 8,
        height: 8,
        borderRadius: '50%',
        backgroundColor: config.color
      }}
      title={config.label}
    />
  );
}
```

### With Reconnect Button
```javascript
function ConnectionStatusWithRetry() {
  const { connectionState } = useWebSocketContext();

  return (
    <div className="connection-status-container">
      <ConnectionStatus />
      {connectionState === 'disconnected' && (
        <button
          onClick={() => wsService.connect()}
          className="btn-small"
        >
          Spróbuj ponownie
        </button>
      )}
    </div>
  );
}
```

## Testing Strategy

```javascript
import { render, screen } from '@testing-library/react';
import ConnectionStatus from './ConnectionStatus';
import { WebSocketProvider } from '../context/WebSocketContext';

const renderWithProvider = (ui, contextValue = {}) => {
  // Mock context or wrap with provider
};

describe('ConnectionStatus', () => {
  test('shows connected state', () => {
    render(<ConnectionStatus />, {
      wrapper: ({ children }) => (
        <MockWebSocketProvider connectionState="connected">
          {children}
        </MockWebSocketProvider>
      )
    });

    expect(screen.getByText('Połączono')).toBeInTheDocument();
  });

  test('shows disconnected state', () => {
    // ...
  });

  test('hides label when showLabel=false', () => {
    render(<ConnectionStatus showLabel={false} />);
    expect(screen.queryByText('Połączono')).not.toBeInTheDocument();
  });

  test('has correct ARIA attributes', () => {
    render(<ConnectionStatus />);
    const status = screen.getByRole('status');
    expect(status).toHaveAttribute('aria-live', 'polite');
  });
});
```

## Dependencies
- Task 05 (WebSocket context)

## Estimated Effort
20-30 minutes

## Files to Create
- `src/components/ConnectionStatus.jsx`

## Files to Modify
- `src/index.css` (add animation keyframes)
