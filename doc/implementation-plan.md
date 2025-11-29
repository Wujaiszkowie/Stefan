To assist with the implementation of "Wspiernik" (Caregiver Assistant), I have analyzed the PRD and Use Cases. Below is a comprehensive implementation plan tailored for Java 21 with Quarkus, as specified in the architecture requirements of the PRD.
<scratchpad> **Analysis & Decision Making:**
Core Requirements:
Context: Local-first, privacy-centric web app for caregivers.
Tech Stack: Java 21, Quarkus (Backend), WebSocket (Communication), SQLite (DB), Docker (Deployment), "Bielnik" LLM (Local Model).
Modules: Survey, Intervention (Crisis scenarios), Support (Emotional), Facts Distiller (Async analysis).
Architecture & Patterns:
Quarkus specific: Use Mutiny for reactive programming (essential for WebSockets and async tasks).
Database: Hibernate ORM with Panache for SQLite interaction.
LLM Integration: OpenAI (Java version) is the best fit for integrating LLMs in a Quarkus environment, or a simple REST Client if Bielnik exposes an OpenAI-compatible API.
WebSocket: Quarkus Websockets Next or standard Jakarta WebSocket.
Dependency Graph:
Database Schema & Entities -> DAO/Repository Layer -> Core Business Logic.
LLM Client -> Orchestrator -> Scenario Logic.
WebSocket Endpoint -> Dispatcher -> Specific Module Handlers.
Async Facts Distiller depends on completed Conversations.
Refinement on "Bielik" LLM:
The PRD mentions "Bielik" running in a container. I will assume it exposes an HTTP API (likely OAI compatible or generic text-generation) and will design an interface to abstract this communication.
Correction:
The user prompt mentioned "Python technologies" in the instruction text, but the PRD explicitly mandates Java 21 and Quarkus. I will strictly follow the PRD (Java/Quarkus) as it represents the source of truth for the architecture.
</scratchpad>
<implementation_plan>
1. Project Overview
   Wspiernik is a privacy-first, local digital assistant for caregivers. It serves three main functions: gathering structured health data (Survey), guiding caregivers through crisis situations (Intervention), and providing emotional support (Support).
   The system relies on a local Large Language Model (LLM) to analyze conversations and distill "facts" (medical history, symptoms) into a structured SQLite database. The architecture is event-driven, utilizing WebSockets for real-time communication between the frontend and the Java/Quarkus backend.
2. Technology Stack
   Language: Java 21 (LTS) - For modern syntax features (Records, Pattern Matching, Virtual Threads).
   Framework: Quarkus (Latest stable) - Chosen for low memory footprint, fast startup, and excellent support for GraalVM/Docker images.
   Reactive Library: Mutiny - Native to Quarkus, essential for handling asynchronous WebSocket messages and non-blocking LLM calls.
   Database: SQLite - Serverless, file-based storage suitable for local deployment.
   ORM: Hibernate ORM with Panache - Simplifies data access with the active record pattern or repository pattern.
   LLM Integration: OpenAI (or Quarkus REST Client) - To interface with the local "Bielnik" LLM container.
   JSON Processing: Jackson - Standard for parsing WebSocket payloads and DB JSON fields.
   Testing: JUnit 5 & RestAssured - Standard Quarkus testing capabilities.
3. Architecture & Design Principles
   Hexagonal Architecture (Simplified): The core domain logic (Intervention, Support) should be decoupled from external interfaces (WebSockets, Database).
   Event-Driven WebSocket Dispatcher: A central WebSocket endpoint receives all messages and routes them to specific "Conversation Handlers" based on the type field.
   Stateful Conversations: Since HTTP is stateless but the use cases are conversational, a session manager will hold the context of the current active conversation in memory (or cached in DB) until it is completed.
   Fact Distillation: The FactsDistiller will run after finishing session of chatting with model
   Interface-based LLM Client: An LlmClient interface will abstract the actual HTTP calls to the Bielnik container, allowing for easy mocking during tests.
4. Core Components/Modules
   Persistence Layer (com.wspiernik.infrastructure.persistence)
   Entities: CaregiverProfile, Conversation, Fact, CrisisScenario, BackendLog.
   Repositories: Panache repositories for database access.
   LLM Integration (com.wspiernik.infrastructure.llm)
   LlmClient: Interface for sending prompts to the local model.
   PromptTemplates: Management of system prompts (Survey, Intervention, etc.).
   Domain Services (com.wspiernik.domain)
   SurveyService: Manages the initial data collection flow.
   InterventionService: Matches keywords to scenarios and manages crisis dialogs.
   SupportService: Handles the emotional support chat flow.
   FactsDistillerService: Async service that processes finished conversations.
   WebSocket API (com.wspiernik.api.websocket)
   WspiernikSocket: The @ServerEndpoint.
   MessageDispatcher: Routes incoming JSON to the correct Service.
   Dto: Request/Response records (e.g., IncomingMessage, OutgoingMessage).
5. Implementation Tasks
   Phase 1: Foundation & Persistence
   Task 001: Project Setup & Docker Config
   Description: Initialize Quarkus project, configure SQLite driver, and create Docker Compose for App + SQLite + local LLM.
   Files/Modules: pom.xml, src/main/resources/application.properties, docker-compose.yml.
   Key Details: Configure quarkus.datasource for SQLite. Enable Hibernate ORM.
   Dependencies: None.
   Complexity: Low.
   Acceptance Criteria: Project compiles; Docker compose starts the app and creates a persistent wspiernik.db file.
   Task 002: Database Schema & Entities
   Description: Implement the entities defined in PRD Section 7 using Hibernate Panache.
   Files/Modules: model/CaregiverProfile.java, model/Conversation.java, model/Fact.java, model/CrisisScenario.java.
   Key Details: Use List<String> or custom objects for JSON fields (mapped via @JdbcTypeCode(SqlTypes.JSON) or String converters).
   Dependencies: Task 001.
   Complexity: Medium.
   Acceptance Criteria: Application starts, tables are auto-generated in SQLite, basic CRUD tests pass.
   Phase 2: Core Logic & LLM
   Task 003: LLM Client Integration
   Description: Create a Rest Client interface to communicate with the "Bielik" container.
   Files/Modules: infrastructure/llm/BielnikClient.java, infrastructure/llm/LlmService.java.
   Key Details: Implement a method String generate(String systemPrompt, String userMessage, String context). Handle timeouts gracefully.
   Dependencies: Task 001.
   Complexity: Medium.
   Acceptance Criteria: Unit test sending a string to a mock endpoint receives a string response.
   Task 004: Scenario Seeding
   Description: Populate CrisisScenario table with the 3 MVP scenarios (Fall, Confusion, Chest Pain) on startup.
   Files/Modules: infrastructure/lifecycle/DataInitializer.java.
   Key Details: Use Quarkus @Observes StartupEvent. Insert scenarios if table is empty.
   Dependencies: Task 002.
   Complexity: Low.
   Acceptance Criteria: Database contains the 3 scenarios after application boot.
   Phase 3: Domain Services (The "Brains")
   Task 005: Survey Module Logic
   Description: Implement logic to manage the step-by-step survey flow.
   Files/Modules: domain/survey/SurveyService.java.
   Key Details: State machine to track which question (Age -> Conditions -> Meds) is next. Save final result to CaregiverProfile.
   Key Details: Inject all facts
   Dependencies: Task 002, Task 003.
   Complexity: Medium.
   Acceptance Criteria: Service takes user input, generates next question via LLM (or static list), and saves profile upon completion.
   Task 006: Intervention Module Logic
   Description: Implement keyword matching and scenario execution.
   Files/Modules: domain/intervention/InterventionService.java.
   Key Details:
   matchScenario(String description): Simple keyword check against DB scenarios.
   processInput(String conversationId, String input): Appends to raw_transcript and asks LLM for next step.
   Dependencies: Task 003, Task 004.
   Complexity: High.
   Acceptance Criteria: Input "upadek" triggers "Fall" scenario; conversation is logged in DB.
   Task 007: Support Module Logic
   Description: Implement the empathetic chat flow.
   Files/Modules: domain/support/SupportService.java.
   Key Details: Inject CaregiverProfile context into the System Prompt so the LLM knows who the patient is.
   Key Details: Inject all facts
   Dependencies: Task 002, Task 003.
   Complexity: Low.
   Acceptance Criteria: Service accepts text and returns empathetic LLM response.
   Phase 4: WebSocket & Asynchronous Processing
   Task 008: WebSocket Infrastructure
   Description: Set up the WebSocket server and the message dispatcher pattern.
   Files/Modules: api/websocket/WspiernikSocket.java, api/websocket/MessageDispatcher.java.
   Key Details: onMessage parses JSON type. Dispatcher routes survey_start to SurveyService, etc.
   Dependencies: Tasks 005, 006, 007.
   Complexity: High.
   Acceptance Criteria: Can connect via WS client (e.g., Postman), send {type: "survey_start"}, and receive {type: "survey_question"}.
   Task 009: Facts Distiller (Async)
   Description: Implement the background job that runs after a conversation ends.
   Files/Modules: domain/facts/FactsDistiller.java.
   Key Details:
   Listens for ConversationCompletedEvent.
   Feeds raw_transcript + existing_facts to LLM.
   Parses JSON response from LLM.
   Saves distinct new facts to Fact table.
   Sends facts_extracted notification via WebSocket.
   Dependencies: Task 003, Task 008.
   Complexity: High.
   Acceptance Criteria: Closing a conversation triggers a log entry showing facts were extracted and saved.
   Phase 5: Logging & Polish
   Task 010: Backend Logging & Error Handling
   Description: Implement the custom logger to SQLite and global exception handling.
   Files/Modules: infrastructure/logging/SqliteLogger.java, api/websocket/ErrorHandler.java.
   Key Details: Catch exceptions in WebSocket flow, log to backend_logs table, return generic error message to client.
   Dependencies: Task 002.
   Complexity: Low.
   Acceptance Criteria: Triggering an error writes a row to backend_logs.
6. Code Organization Guidelines
   Directory Structure:
   text
   src/main/java/com/wspiernik/
   ├── api/
   │   ├── dto/           # JSON Records (IncomingMessage, OutgoingMessage)
   │   └── websocket/     # Socket endpoint and Dispatcher
   ├── domain/
   │   ├── survey/        # Survey logic
   │   ├── intervention/  # Intervention logic
   │   ├── support/       # Support logic
   │   └── facts/         # Distiller logic
   ├── infrastructure/
   │   ├── llm/           # Bielnik integration
   │   ├── persistence/   # Hibernate Entities & Repositories
   │   └── lifecycle/     # Startup scripts
   └── utils/             # JSON parsers, Date formatters

Naming Conventions:
Classes: PascalCase (e.g., SurveyService).
Methods/Variables: camelCase (e.g., processMessage).
Constants: SCREAMING_SNAKE_CASE.
DTOs: Use Java Records (e.g., record SurveyResponse(String text) {}).
Code Style:
Use Java Records for all DTOs and immutable data carriers.
Use Constructor Injection (Quarkus standard) instead of @Inject on fields.
Use text blocks (""") for large SQL queries or System Prompts.
7. Implementation Sequence
   Foundation: Tasks 001 (Setup), 002 (DB), 004 (Seeds).
   Core Services: Task 003 (LLM Client), Task 010 (Logging).
   Domain Logic (Parallelizable):
   Developer A: Task 005 (Survey).
   Developer B: Task 006 (Intervention).
   Developer C: Task 007 (Support).
   Integration: Task 008 (WebSockets).
   Async Processing: Task 009 (Facts Distiller).
8. Maintenance & Extensibility Considerations
   Adding Scenarios: New scenarios can be added purely via SQL/Database inserts without changing Java code, thanks to the generic InterventionService logic.
   Swapping LLMs: The LlmClient interface allows switching from "Bielnik" to OpenAI or Claude seamlessly by changing the implementation class or configuration.
   Phase 2 Expansion:
   Overseer Module: Can be implemented as a middleware in the InterventionService before sending the LLM response to the user.
   Mobile App: The WebSocket API is client-agnostic. A Flutter/React Native app can connect to the same endpoint.
   Troubleshooting: The backend_logs table allows debugging issues on the user's local machine without needing cloud telemetry.
   </implementation_plan>
