# Step 01: Foundation & Configuration (Tasks 1-3)

**Status:** COMPLETED
**Tasks:** 1, 2, 3

---

## Task 1: Project Configuration & Dependencies

### Goal
Update `pom.xml` with all required dependencies for Wspiernik backend.

### Changes
- **File:** `backend-java/pom.xml`
- **Actions:**
  - Change groupId from `org.acme` to `com.wspiernik`
  - Change artifactId to `wspiernik-backend`
  - Add dependencies:
    - `quarkus-websockets-next` - WebSocket support
    - `quarkus-hibernate-orm-panache` - ORM with active record pattern
    - `sqlite-jdbc` (xerial) - SQLite JDBC driver
    - `hibernate-community-dialects` - SQLite dialect
    - `quarkus-rest-client` + `quarkus-rest-client-jackson` - LLM HTTP calls
    - `quarkus-scheduler` - Async processing
    - `quarkus-smallrye-health` - Health checks

### Acceptance Criteria
- [x] Project compiles with new dependencies
- [x] Package renamed to `com.wspiernik`

---

## Task 2: Application Configuration

### Goal
Configure `application.properties` for SQLite, LLM, WebSocket, and logging.

### Changes
- **File:** `backend-java/src/main/resources/application.properties`
- **Configuration Sections:**
  1. **Database (SQLite):**
     - Driver: `org.sqlite.JDBC`
     - URL: `jdbc:sqlite:wspiernik.db`
     - Dialect: `SQLiteDialect`
     - Schema generation: `update`
  2. **LLM (Bielnik):**
     - Base URL: configurable (default `http://localhost:11434`)
     - Model name, timeout, max tokens, temperature
  3. **WebSocket:**
     - Auto-ping interval: 30s
  4. **CORS:**
     - Allow frontend origins (localhost:3000, localhost:5173)
  5. **Logging:**
     - INFO level default, DEBUG for `com.wspiernik`

### Acceptance Criteria
- [x] Application starts without configuration errors
- [x] SQLite database file created on startup

---

## Task 3: Database Entities

### Goal
Create JPA entities matching the PRD database schema.

### Changes
- **New Files:**
  - `src/main/java/com/wspiernik/infrastructure/persistence/entity/CaregiverProfile.java`
  - `src/main/java/com/wspiernik/infrastructure/persistence/entity/Conversation.java`
  - `src/main/java/com/wspiernik/infrastructure/persistence/entity/Fact.java`
  - `src/main/java/com/wspiernik/infrastructure/persistence/entity/CrisisScenario.java`
  - `src/main/java/com/wspiernik/infrastructure/persistence/entity/CaregiverSupportLog.java`
  - `src/main/java/com/wspiernik/infrastructure/persistence/entity/BackendLog.java`

### Entity Details

#### CaregiverProfile
| Field | Type | Notes |
|-------|------|-------|
| id | Long | Primary key |
| caregiverId | String | Unique identifier |
| wardAge | Integer | Ward's age |
| wardConditions | String | JSON array of conditions |
| wardMedications | String | JSON array of medications |
| wardMobilityLimits | String | JSON mobility info |
| createdAt | LocalDateTime | |
| updatedAt | LocalDateTime | |

#### Conversation
| Field | Type | Notes |
|-------|------|-------|
| id | Long | Primary key |
| caregiverId | String | |
| conversationType | String | "survey", "intervention", "support" |
| scenarioType | String | Nullable, for intervention |
| rawTranscript | String | Full conversation text |
| startedAt | LocalDateTime | |
| endedAt | LocalDateTime | |
| factsExtracted | Boolean | Default false |
| createdAt | LocalDateTime | |

#### Fact
| Field | Type | Notes |
|-------|------|-------|
| id | Long | Primary key |
| conversationId | Long | FK to Conversation |
| factType | String | "symptom", "medication", "event", etc. |
| factValue | String | The extracted fact |
| severity | Integer | 1-10, nullable |
| context | String | Additional context |
| extractedAt | LocalDateTime | |
| createdAt | LocalDateTime | |

#### CrisisScenario
| Field | Type | Notes |
|-------|------|-------|
| id | Long | Primary key |
| scenarioKey | String | Unique: "fall", "confusion", "chest_pain" |
| name | String | Display name |
| triggerKeywords | String | JSON array |
| questionsSequence | String | JSON array |
| systemPrompt | String | LLM prompt |
| createdAt | LocalDateTime | |

#### CaregiverSupportLog
| Field | Type | Notes |
|-------|------|-------|
| id | Long | Primary key |
| caregiverId | String | |
| conversationId | Long | FK to Conversation |
| stressLevel | Integer | 1-10 |
| needs | String | JSON |
| createdAt | LocalDateTime | |

#### BackendLog
| Field | Type | Notes |
|-------|------|-------|
| id | Long | Primary key |
| timestamp | LocalDateTime | |
| level | String | "INFO", "WARNING", "ERROR" |
| module | String | |
| message | String | |
| details | String | JSON |

### Acceptance Criteria
- [x] All entities created with proper JPA annotations
- [x] Tables auto-generated on application startup
- [x] Entities extend PanacheEntityBase for Panache support (using IDENTITY generation)

---

## Implementation Notes

- Using `PanacheEntity` base class for automatic ID generation
- JSON fields stored as TEXT in SQLite
- Timestamps use `LocalDateTime`
- All entities in `com.wspiernik.infrastructure.persistence.entity` package
