# Step 03: LLM Integration (Tasks 6-8)

**Status:** COMPLETED
**Tasks:** 6, 7, 8

---

## Task 6: LLM Client Interface & DTOs

### Goal
Define the LLM client interface and data transfer objects for communication.

### Changes
- **New Files:**
  - `src/main/java/com/wspiernik/infrastructure/llm/LlmClient.java` (interface)
  - `src/main/java/com/wspiernik/infrastructure/llm/dto/LlmMessage.java`
  - `src/main/java/com/wspiernik/infrastructure/llm/dto/LlmRequest.java`
  - `src/main/java/com/wspiernik/infrastructure/llm/dto/LlmResponse.java`
  - `src/main/java/com/wspiernik/infrastructure/llm/dto/LlmChoice.java`

### Interface Definition
```java
public interface LlmClient {
    String generate(String systemPrompt, String userMessage);
    String generateWithContext(String systemPrompt, String userMessage, List<LlmMessage> history);
    String generateWithHistory(List<LlmMessage> messages);
}
```

### DTOs (OpenAI Compatible Format)
```java
// Request
record LlmRequest(
    String model,
    List<LlmMessage> messages,
    double temperature,
    int max_tokens
) {}

// Message
record LlmMessage(
    String role,  // "system", "user", "assistant"
    String content
) {}

// Response
record LlmResponse(
    List<LlmChoice> choices
) {}

// Choice
record LlmChoice(
    LlmMessage message
) {}
```

### Acceptance Criteria
- [x] Interface defined with clear contract
- [x] DTOs match OpenAI API format
- [x] Records used for immutability

---

## Task 7: Bielnik LLM Implementation

### Goal
Implement the LLM client using Quarkus REST Client.

### Changes
- **New Files:**
  - `src/main/java/com/wspiernik/infrastructure/llm/BielnikApi.java` (REST Client interface)
  - `src/main/java/com/wspiernik/infrastructure/llm/BielnikClient.java` (implementation)

### REST Client Interface
```java
@Path("/v1")
@RegisterRestClient
public interface BielnikApi {
    @POST
    @Path("/chat/completions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    LlmResponse chatCompletion(LlmRequest request);
}
```

### Implementation Details
- Inject configuration (model, temperature, max_tokens)
- Handle timeouts gracefully (return error message, not exception)
- Log all LLM requests/responses at DEBUG level
- Implement retry logic for transient failures (optional)

### Acceptance Criteria
- [x] REST Client configured in application.properties
- [x] LlmClient implementation is injectable
- [x] Timeout handling works correctly
- [x] Errors logged properly

---

## Task 8: System Prompts Management

### Goal
Create centralized prompt templates for all modules.

### Changes
- **New File:**
  - `src/main/java/com/wspiernik/infrastructure/llm/PromptTemplates.java`

### Prompts to Define

#### Survey Prompt
```
Ty jesteś asystentem do zbierania informacji zdrowotnych...
```

#### Intervention Prompt (Template with Placeholders)
```
Ty jesteś asystentem wspomagającym opiekuna w sytuacji kryzysowej.
Profil podopiecznego: {profile_json}
Znane fakty: {facts_json}
Scenariusz: {scenario_name}
...
```

#### Support Prompt
```
Ty jesteś wspierającym asystentem dla opiekunów...
```

#### Facts Distiller Prompt
```
Przeanalizuj poniższy zapis konwersacji i wyekstrahuj kluczowe fakty...
```

### Implementation
- Use Java text blocks (""") for multi-line prompts
- Create methods to inject context:
  - `buildSurveyPrompt()`
  - `buildInterventionPrompt(CaregiverProfile profile, List<Fact> facts, CrisisScenario scenario)`
  - `buildSupportPrompt(CaregiverProfile profile, List<Fact> facts)`
  - `buildFactsDistillerPrompt(String transcript, List<Fact> existingFacts)`

### Acceptance Criteria
- [x] All prompts defined as per PRD Section 9
- [x] Context injection methods work correctly
- [x] Prompts are configurable (can be overridden if needed)

---

## Implementation Notes

- Using Quarkus REST Client (not manual HTTP calls)
- OpenAI-compatible API assumed (POST /v1/chat/completions)
- System prompts in Polish as per PRD
