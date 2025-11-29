# Step 02: Repository Layer & Data Initialization (Tasks 4-5)

**Status:** PENDING
**Tasks:** 4, 5

---

## Task 4: Repository Layer

### Goal
Create Panache repositories for each entity with custom query methods.

### Changes
- **New Files:**
  - `src/main/java/com/wspiernik/infrastructure/persistence/repository/CaregiverProfileRepository.java`
  - `src/main/java/com/wspiernik/infrastructure/persistence/repository/ConversationRepository.java`
  - `src/main/java/com/wspiernik/infrastructure/persistence/repository/FactRepository.java`
  - `src/main/java/com/wspiernik/infrastructure/persistence/repository/CrisisScenarioRepository.java`
  - `src/main/java/com/wspiernik/infrastructure/persistence/repository/CaregiverSupportLogRepository.java`
  - `src/main/java/com/wspiernik/infrastructure/persistence/repository/BackendLogRepository.java`

### Repository Methods

#### CaregiverProfileRepository
```java
Optional<CaregiverProfile> findByCaregiverId(String caregiverId)
```

#### ConversationRepository
```java
List<Conversation> findByCaregiverId(String caregiverId)
List<Conversation> findByConversationType(String type)
List<Conversation> findUnprocessedForFactsExtraction()
```

#### FactRepository
```java
List<Fact> findByConversationId(Long conversationId)
List<Fact> findByFactType(String factType)
List<Fact> findAllOrderByCreatedAtDesc(int limit)
long countAll()
```

#### CrisisScenarioRepository
```java
Optional<CrisisScenario> findByScenarioKey(String key)
List<CrisisScenario> listAll()
```

#### CaregiverSupportLogRepository
```java
List<CaregiverSupportLog> findByCaregiverId(String caregiverId)
```

#### BackendLogRepository
```java
void logInfo(String module, String message, String details)
void logError(String module, String message, String details)
List<BackendLog> findByLevel(String level, int limit)
```

### Acceptance Criteria
- [ ] All repositories created as `@ApplicationScoped` beans
- [ ] Custom query methods implemented
- [ ] Repositories injectable via CDI

---

## Task 5: Database Initialization

### Goal
Seed the database with 3 MVP crisis scenarios on application startup.

### Changes
- **New File:**
  - `src/main/java/com/wspiernik/infrastructure/lifecycle/DataInitializer.java`

### Scenarios to Seed

#### 1. Fall (Upadek)
```json
{
  "scenarioKey": "fall",
  "name": "Upadek",
  "triggerKeywords": ["upadek", "upadł", "upadła", "przewrócił", "spadł", "spadła"],
  "questionsSequence": [
    "Czy podopieczny stracił przytomność?",
    "Gdzie dokładnie boli?",
    "Czy może ruszać rękami i nogami?",
    "Czy jest krwawienie?",
    "Jaki jest teraz stan świadomości?"
  ],
  "systemPrompt": "..." // Full prompt from PRD
}
```

#### 2. Mental Confusion (Zamieszanie Umysłowe)
```json
{
  "scenarioKey": "confusion",
  "name": "Zamieszanie Umysłowe",
  "triggerKeywords": ["zamieszanie", "dezorientacja", "zdezorientowany", "nie poznaje", "mówi bez sensu", "nonsens"],
  "questionsSequence": [
    "Od kiedy zauważyłeś/aś zmianę?",
    "Czy podopieczny poznaje Cię?",
    "Czy mówi rzeczy bez sensu?",
    "Czy jest niespokojny lub agresywny?",
    "Czy miał dzisiaj gorączkę?"
  ],
  "systemPrompt": "..."
}
```

#### 3. Chest Pain (Ból w Klatce Piersiowej)
```json
{
  "scenarioKey": "chest_pain",
  "name": "Ból w Klatce Piersiowej",
  "triggerKeywords": ["ból w klatce", "ból serca", "boli serce", "klatka piersiowa", "duszność", "dusi się"],
  "questionsSequence": [
    "Jak silny jest ból w skali 1-10?",
    "Czy ból promieniuje do ramienia lub szczęki?",
    "Czy występuje duszność?",
    "Czy są nudności lub wymioty?",
    "Czy podopieczny jest spocony?"
  ],
  "systemPrompt": "..."
}
```

### Implementation Details
- Use `@Observes StartupEvent` to run on application startup
- Check if scenarios exist before inserting (avoid duplicates)
- Log initialization status

### Acceptance Criteria
- [ ] DataInitializer runs on startup
- [ ] 3 scenarios inserted if table is empty
- [ ] Duplicate scenarios not inserted on restart
- [ ] Startup logs show initialization status

---

## Implementation Notes

- Repositories use Panache Repository pattern (not Active Record)
- DataInitializer is idempotent - safe to run multiple times
- System prompts stored as TEXT (can be long)
