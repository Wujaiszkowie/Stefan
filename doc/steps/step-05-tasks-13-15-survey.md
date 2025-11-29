# Step 05: Survey Module (Tasks 13-15)

**Status:** DONE
**Tasks:** 13, 14, 15

---

## Task 13: Survey Service Core

### Goal
Create the Survey service with state machine for step progression.

### Changes
- **New Files:**
  - `src/main/java/com/wspiernik/domain/survey/SurveyService.java`
  - `src/main/java/com/wspiernik/domain/survey/SurveyStep.java` (enum)
  - `src/main/java/com/wspiernik/domain/survey/SurveyState.java`

### Survey Steps (State Machine)
```java
enum SurveyStep {
    WARD_AGE,           // Step 1: Age
    WARD_CONDITIONS,    // Step 2: Conditions/diseases
    WARD_MEDICATIONS,   // Step 3: Medications
    WARD_MOBILITY,      // Step 4: Mobility limits
    WARD_OTHER,         // Step 5: Other important info
    CONFIRMATION,       // Step 6: Summary confirmation
    COMPLETED           // Final state
}
```

### Service Methods
```java
SurveyState startSurvey(String caregiverId);
SurveyState processAnswer(String sessionId, String answer);
CaregiverProfile completeSurvey(String sessionId);
String getCurrentQuestion(SurveyState state);
```

### Survey State
```java
record SurveyState(
    String sessionId,
    SurveyStep currentStep,
    Map<SurveyStep, String> answers,
    List<LlmMessage> conversationHistory
) {}
```

### Acceptance Criteria
- [x] State machine transitions correctly
- [x] All required data points collected
- [x] State preserved across messages

---

## Task 14: Survey LLM Integration

### Goal
Generate conversational questions and parse responses via LLM.

### Changes
- **Updates to:** `SurveyService.java`
- **New File:** `src/main/java/com/wspiernik/domain/survey/SurveyLlmHandler.java`

### LLM Interaction Flow
1. System prompt sets context (data collection assistant)
2. Each step has a topic but LLM generates natural question
3. LLM can ask follow-up questions if answer is unclear
4. Extract structured data from conversational responses

### Question Generation
```java
String generateQuestion(SurveyStep step, Map<SurveyStep, String> previousAnswers) {
    // Build context from previous answers
    // Ask LLM to generate natural question for current step
    // Return question text
}
```

### Response Processing
```java
ProcessedAnswer processResponse(SurveyStep step, String userResponse) {
    // Ask LLM to extract structured data
    // Determine if follow-up needed
    // Return: {data, needsFollowUp, followUpQuestion}
}
```

### Acceptance Criteria
- [x] Questions are conversational, not form-like
- [x] Follow-up questions work when answers are unclear
- [x] Data extracted correctly from natural language

---

## Task 15: Survey Completion

### Goal
Save profile to database and trigger facts extraction.

### Changes
- **Updates to:** `SurveyService.java`
- **New Event:** `src/main/java/com/wspiernik/domain/events/ConversationCompletedEvent.java`

### Completion Flow
1. Validate all required data collected
2. Create `CaregiverProfile` entity
3. Save to database
4. Create `Conversation` record (type: "survey")
5. Fire `ConversationCompletedEvent` for Facts Distiller
6. Return completion response

### Profile Creation
```java
CaregiverProfile createProfile(SurveyState state) {
    var profile = new CaregiverProfile();
    profile.caregiverId = generateCaregiverId();
    profile.wardAge = parseAge(state.answers().get(WARD_AGE));
    profile.wardConditions = toJson(state.answers().get(WARD_CONDITIONS));
    profile.wardMedications = toJson(state.answers().get(WARD_MEDICATIONS));
    profile.wardMobilityLimits = state.answers().get(WARD_MOBILITY);
    profile.createdAt = LocalDateTime.now();
    profile.persist();
    return profile;
}
```

### WebSocket Response
```json
{
  "type": "survey_completed",
  "payload": {
    "profile_id": "prof_001",
    "facts_saved": 5
  }
}
```

### Acceptance Criteria
- [x] Profile saved to database
- [x] Conversation transcript saved
- [ ] Facts Distiller triggered (not yet implemented)
- [x] Completion message sent to client

---

## Implementation Notes

- Survey is required before other modules (first-time use)
- 8-10 essential questions as per PRD
- No edit functionality in MVP
- Conversational flow, not questionnaire
