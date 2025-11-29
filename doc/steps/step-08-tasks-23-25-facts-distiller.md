# Step 08: Facts Distiller (Tasks 23-25)

**Status:** DONE
**Tasks:** 23, 24, 25

---

## Task 23: Facts Distiller Service

### Goal
Create asynchronous service to extract facts from conversations.

### Changes
- **New Files:**
  - `src/main/java/com/wspiernik/domain/facts/FactsDistillerService.java`
  - `src/main/java/com/wspiernik/domain/events/ConversationCompletedEvent.java`

### Event Definition
```java
public record ConversationCompletedEvent(
    Long conversationId,
    String caregiverId,
    String conversationType,
    String rawTranscript,
    String webSocketConnectionId  // For async notification
) {}
```

### Service Structure
```java
@ApplicationScoped
public class FactsDistillerService {

    @Inject
    Event<FactsExtractedEvent> factsExtractedEvent;

    void onConversationCompleted(@ObservesAsync ConversationCompletedEvent event) {
        // Process asynchronously
        // Extract facts
        // Save to database
        // Fire notification event
    }
}
```

### Async Processing
- Use `@ObservesAsync` for non-blocking processing
- Don't block the WebSocket response
- Log processing start/end for debugging

### Acceptance Criteria
- [x] Event listener registered
- [x] Processing is truly asynchronous
- [x] Errors don't crash the application

---

## Task 24: Facts Extraction Logic

### Goal
Use LLM to extract structured facts from conversation transcripts.

### Changes
- **New File:** `src/main/java/com/wspiernik/domain/facts/FactsExtractor.java`

### System Prompt for Extraction
```
Przeanalizuj poniższy zapis konwersacji i wyekstrahuj kluczowe fakty.

Fakty powinny być w formacie JSON:
[
  {
    "type": "symptom|medication|event|condition|limitation",
    "value": "opis faktu",
    "severity": 1-10,
    "context": "dodatkowy kontekst"
  }
]

Typy faktów:
- symptom: objawy zdrowotne (np. "ból głowy", "zawroty głowy")
- medication: leki (np. "Aspirin 500mg 2x dziennie")
- event: zdarzenia (np. "upadek ze schodów")
- condition: stany/choroby (np. "cukrzyca")
- limitation: ograniczenia (np. "trudności z chodzeniem")

WAŻNE:
- Tylko wyciągaj fakty które NIE są już znane
- Sprawdź poniższą listę znanych faktów

Znane fakty:
{existing_facts_json}

Transkrypt rozmowy:
{transcript}

Nowe fakty (JSON array, lub [] jeśli brak nowych):
```

### Extraction Process
```java
List<ExtractedFact> extractFacts(String transcript, List<Fact> existingFacts) {
    String prompt = buildExtractionPrompt(transcript, existingFacts);
    String response = llmClient.generate(FACTS_DISTILLER_PROMPT, prompt);

    // Parse JSON response
    List<ExtractedFact> extracted = parseFactsJson(response);

    // Filter duplicates (double-check against existing)
    return filterDuplicates(extracted, existingFacts);
}
```

### Extracted Fact DTO
```java
record ExtractedFact(
    String type,
    String value,
    Integer severity,
    String context
) {}
```

### Deduplication Logic
- Compare by type + normalized value
- Consider facts similar if >80% string similarity
- Log skipped duplicates for debugging

### Acceptance Criteria
- [x] LLM prompt generates valid JSON
- [x] Facts parsed correctly
- [x] Duplicates filtered out
- [x] Empty array handled (no new facts)

---

## Task 25: Facts Persistence & Notification

### Goal
Save extracted facts and notify client via WebSocket.

### Changes
- **Updates to:** `FactsDistillerService.java`
- **New Event:** `src/main/java/com/wspiernik/domain/events/FactsExtractedEvent.java`

### Persistence Flow
```java
List<Fact> saveFacts(Long conversationId, List<ExtractedFact> extracted) {
    List<Fact> saved = new ArrayList<>();

    for (ExtractedFact ef : extracted) {
        Fact fact = new Fact();
        fact.conversationId = conversationId;
        fact.factType = ef.type();
        fact.factValue = ef.value();
        fact.severity = ef.severity();
        fact.context = ef.context();
        fact.extractedAt = LocalDateTime.now();
        fact.createdAt = LocalDateTime.now();
        fact.persist();
        saved.add(fact);
    }

    // Update conversation flag
    conversationRepository.markFactsExtracted(conversationId);

    return saved;
}
```

### WebSocket Notification
```java
void notifyClient(String connectionId, Long conversationId, List<Fact> facts) {
    OutgoingMessage message = new OutgoingMessage(
        "facts_extracted",
        new FactsExtractedPayload(
            conversationId,
            facts.size(),
            facts.stream().map(this::toDto).toList()
        )
    );

    webSocketNotifier.send(connectionId, message);
}
```

### Notification Payload
```json
{
  "type": "facts_extracted",
  "payload": {
    "conversation_id": "conv_789",
    "facts_count": 3,
    "facts": [
      {
        "id": 1,
        "type": "event",
        "value": "upadek ze schodów",
        "severity": 8,
        "context": "strata przytomności na kilka sekund"
      },
      {
        "id": 2,
        "type": "symptom",
        "value": "ból nogi i barku",
        "severity": 6,
        "context": "może ruszać, ale boli"
      }
    ]
  }
}
```

### Edge Cases
- No facts extracted: send notification with `facts_count: 0`
- Connection closed: log warning, don't fail
- LLM timeout: mark conversation as `facts_extracted: false`, retry later

### Acceptance Criteria
- [x] Facts saved to database
- [x] Conversation marked as processed
- [x] WebSocket notification sent
- [x] Handles edge cases gracefully

---

## Implementation Notes

- Processing is fully asynchronous (non-blocking)
- Facts linked to conversation via `conversation_id`
- Notification requires active WebSocket connection
- If connection lost, facts still saved (just no notification)
