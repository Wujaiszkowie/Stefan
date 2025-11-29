# Step 07: Support Module (Tasks 20-22)

**Status:** PENDING
**Tasks:** 20, 21, 22

---

## Task 20: Support Service Core

### Goal
Create the emotional support service for caregivers.

### Changes
- **New Files:**
  - `src/main/java/com/wspiernik/domain/support/SupportService.java`
  - `src/main/java/com/wspiernik/domain/support/SupportState.java`

### Support State
```java
record SupportState(
    String sessionId,
    Long conversationId,
    String caregiverId,
    List<LlmMessage> conversationHistory,
    CaregiverProfile profile,
    List<Fact> existingFacts,
    LocalDateTime startedAt
) {}
```

### Service Methods
```java
SupportState startSupport(String caregiverId);
SupportState processMessage(String sessionId, String message);
SupportCompletionResult completeSupport(String sessionId);
```

### Flow
1. **Start:** Load profile + facts, create conversation, send opening message
2. **Process:** Add to history, generate empathetic response via LLM
3. **Complete:** Save transcript, create support log, trigger facts extraction

### Opening Message
```
"Wiem że teraz jest trudno. Chciałbym Ci pomóc. Jak się teraz czujesz?"
```

### Acceptance Criteria
- [ ] Profile and facts loaded on start
- [ ] Conversation state maintained
- [ ] Empathetic tone throughout

---

## Task 21: Support LLM Integration

### Goal
Generate empathetic responses using LLM with Support prompt.

### Changes
- **New File:** `src/main/java/com/wspiernik/domain/support/SupportLlmHandler.java`

### System Prompt
```
Ty jesteś wspierającym asystentem dla opiekunów.

Kontekst podopiecznego:
- Wiek: {ward_age}
- Choroby: {ward_conditions}

Ostatnie zdarzenia: {recent_facts}

Twoja rola to wspierać emocjonalnie, uspokajać i pomagać.

Bądź:
- Empatyczny (potwierdzaj uczucia)
- Wspierający (daj nadzieję)
- Praktyczny (zasugeruj kroki jeśli możliwe)
- Pozytywny (ale autentyczny)

WAŻNE:
- NIE udzielaj rad medycznych
- Jeśli opiekun pyta o medycynę, zalecaj kontakt z lekarzem
- Słuchaj i pozwól opiekunowi wyrazić emocje
- Po 5-10 minutach rozmowy, zaproponuj podsumowanie
```

### Response Generation
```java
String generateResponse(SupportState state, String userMessage) {
    // Add user message to history
    // Build system prompt with context
    // Generate empathetic response
    // Check for session length (suggest wrap-up after ~5 min)
}
```

### Tone Guidelines
- Use validating phrases: "Rozumiem że to dla Ciebie trudne..."
- Acknowledge feelings before offering advice
- Simple language, no medical jargon
- Offer concrete steps when appropriate

### Acceptance Criteria
- [ ] Responses are empathetic and supportive
- [ ] No medical advice given
- [ ] Context (profile, facts) influences responses
- [ ] Session length tracked

---

## Task 22: Support Completion

### Goal
Save session data and trigger facts extraction.

### Changes
- **Updates to:** `SupportService.java`

### Completion Flow
1. Build raw transcript from conversation history
2. Update `Conversation` entity (type: "support")
3. Extract stress indicators from conversation
4. Create `CaregiverSupportLog` entry
5. Fire `ConversationCompletedEvent`
6. Return completion response

### Stress Level Extraction
```java
int extractStressLevel(SupportState state) {
    String prompt = """
        Na podstawie poniższej rozmowy, oceń poziom stresu opiekuna
        w skali 1-10 (1 = spokojny, 10 = bardzo zestresowany).

        Rozmowa:
        %s

        Odpowiedz tylko liczbą.
        """.formatted(state.rawTranscript());

    return Integer.parseInt(llmClient.generate(prompt, "").trim());
}
```

### Support Log Creation
```java
CaregiverSupportLog createSupportLog(SupportState state, int stressLevel) {
    var log = new CaregiverSupportLog();
    log.caregiverId = state.caregiverId();
    log.conversationId = state.conversationId();
    log.stressLevel = stressLevel;
    log.needs = extractNeeds(state);  // JSON of identified needs
    log.createdAt = LocalDateTime.now();
    log.persist();
    return log;
}
```

### WebSocket Response
```json
{
  "type": "support_completed",
  "payload": {
    "conversation_id": "conv_101",
    "facts_extraction": "processing"
  }
}
```

### Acceptance Criteria
- [ ] Conversation saved to database
- [ ] Support log created with stress level
- [ ] Facts Distiller triggered
- [ ] Client notified of completion

---

## Implementation Notes

- Support is reactive (user-initiated)
- No proactive check-ins in MVP
- 5-10 minute typical session length
- Caregiver's emotional state may be extracted as facts
