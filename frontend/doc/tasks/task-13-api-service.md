# Task 13: REST API Service

## Batch: 1 (zadanie 1/2)

## Cel
Utworzenie serwisu REST API do komunikacji z backendem, w pierwszej kolejności do sprawdzania statusu onboardingu.

## Plik
`src/services/api.js`

## Wymagania

1. Bazowa konfiguracja URL z `VITE_API_URL` lub fallback do `localhost:8080`
2. Funkcja `checkOnboardingStatus()`:
   - Metoda: `HEAD`
   - Endpoint: `/api/fact`
   - Return: `true` jeśli status 200, `false` jeśli 204
   - Error handling: przy błędzie zwróć `true` (nie blokuj użytkownika)

## API Backend

```java
@HEAD
@Path("/api/fact")
public Response facts() {
    return factRepository.hasAnyFacts()
        ? Response.ok().build()      // 200 - ma fakty
        : Response.noContent().build(); // 204 - brak faktów
}
```

## Implementacja

```javascript
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export async function checkOnboardingStatus() {
  try {
    const response = await fetch(`${API_BASE_URL}/api/fact`, {
      method: 'HEAD',
    });
    return response.status === 200;
  } catch (error) {
    console.error('[API] Failed to check onboarding status:', error);
    return true; // fail-safe
  }
}
```

## Testy manualne

- [ ] Backend włączony, są fakty → zwraca `true`
- [ ] Backend włączony, brak faktów → zwraca `false`
- [ ] Backend wyłączony → zwraca `true` (fail-safe)

## Zależności
- Brak (nowy plik)

## Następne zadanie
Task 14: OnboardingContext
