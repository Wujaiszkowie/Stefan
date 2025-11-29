# Step 06: Intervention Module (Tasks 16-19)

**Status:** DONE
**Tasks:** 16, 17, 18, 19

---

## Task 16: Scenario Matching

### Goal
Match user's crisis description to predefined scenarios.

### Changes
- **New Files:**
  - `src/main/java/com/wspiernik/domain/intervention/ScenarioMatcher.java`

### Matching Algorithm
1. Load all scenarios from database
2. Tokenize user input (lowercase, remove punctuation)
3. Check each scenario's trigger keywords
4. Return first match or null for generic handling

```java
Optional<CrisisScenario> matchScenario(String description) {
    String normalized = description.toLowerCase().trim();

    for (CrisisScenario scenario : scenarioRepository.listAll()) {
        List<String> keywords = parseKeywords(scenario.triggerKeywords);
        for (String keyword : keywords) {
            if (normalized.contains(keyword)) {
                return Optional.of(scenario);
            }
        }
    }
    return Optional.empty();
}
```

### Scenario Keywords (from DB)
| Scenario | Keywords (PL) |
|----------|---------------|
| Fall | upadek, upadł, upadła, przewrócił, spadł |
| Confusion | zamieszanie, dezorientacja, nie poznaje, nonsens |
| Chest Pain | ból w klatce, ból serca, duszność, dusi się |

### Acceptance Criteria
- [x] Keywords loaded from database
- [x] Matching is case-insensitive
- [x] Unmatched input handled gracefully

---

## Task 17: Intervention Service Core

### Goal
Create the main intervention service for crisis handling.

### Changes
- **New Files:**
  - `src/main/java/com/wspiernik/domain/intervention/InterventionService.java`
  - `src/main/java/com/wspiernik/domain/intervention/InterventionState.java`

### Intervention State
```java
record InterventionState(
    String sessionId,
    Long conversationId,
    CrisisScenario scenario,
    int currentStep,
    List<String> collectedInfo,
    List<LlmMessage> conversationHistory,
    CaregiverProfile profile,
    List<Fact> existingFacts
) {}
```

### Service Methods
```java
InterventionState startIntervention(String caregiverId, String description);
InterventionState processResponse(String sessionId, String response);
String completeIntervention(String sessionId);
```

### Flow
1. **Start:** Match scenario, load profile + facts, create conversation
2. **Process:** Add to history, generate next question via LLM
3. **Complete:** Save transcript, trigger facts extraction, return summary

### Acceptance Criteria
- [x] Scenario matched or generic flow used
- [x] Profile context injected into prompts
- [x] Conversation state maintained

---

## Task 18: Intervention LLM Integration

### Goal
Generate scenario-specific questions using LLM.

### Changes
- **New File:** `src/main/java/com/wspiernik/domain/intervention/InterventionLlmHandler.java`

### System Prompt Structure
```
Ty jesteś asystentem wspomagającym opiekuna w sytuacji: {scenario_name}

Profil podopiecznego:
- Wiek: {ward_age}
- Choroby: {ward_conditions}
- Leki: {ward_medications}

Znane fakty: {facts_summary}

Twoje zadanie:
1. Zadaj pytania aby zrozumieć sytuację
2. Zbierz informacje zgodnie z listą: {questions_sequence}
3. Bądź konkretny i rzeczowy
4. Po zebraniu danych zasugeruj działanie
5. Powiedz "INTERVENTION_COMPLETE" gdy skończysz
```

### Question Generation
```java
String generateNextQuestion(InterventionState state) {
    // Build system prompt with profile + facts
    // Include conversation history
    // Ask LLM for next question or summary
}
```

### Response Detection
- Detect "INTERVENTION_COMPLETE" in LLM response
- Parse summary if intervention complete
- Continue questioning otherwise

### Acceptance Criteria
- [x] Questions follow scenario sequence
- [x] Profile context used appropriately
- [x] Completion detected correctly

---

## Task 19: Intervention Completion

### Goal
Save transcript, generate summary, trigger facts extraction.

### Changes
- **Updates to:** `InterventionService.java`

### Completion Flow
1. Build raw transcript from conversation history
2. Update `Conversation` entity with transcript and end time
3. Generate summary using LLM
4. Fire `ConversationCompletedEvent`
5. Return completion response to client

### Summary Generation
```java
String generateSummary(InterventionState state) {
    String prompt = """
        Podsumuj poniższą rozmowę interwencyjną w 3-4 zdaniach:
        %s

        Uwzględnij: główny problem, zebrane informacje, sugestie działań.
        """.formatted(state.rawTranscript());

    return llmClient.generate(prompt, "");
}
```

### WebSocket Responses
```json
// Completion
{
  "type": "intervention_completed",
  "payload": {
    "conversation_id": "conv_789",
    "facts_extraction": "processing",
    "summary": "..."
  }
}

// Async notification (after facts extraction)
{
  "type": "facts_extracted",
  "payload": {
    "conversation_id": "conv_789",
    "facts_count": 3,
    "facts": [...]
  }
}
```

### Acceptance Criteria
- [x] Transcript saved to database
- [x] Summary generated
- [ ] Facts Distiller triggered asynchronously (not yet implemented)
- [x] Client notified of completion

---

## Implementation Notes

- 3 predefined scenarios in MVP
- Generic flow for unmatched crises
- Profile and facts always included in context
- Transcript stored as raw text
