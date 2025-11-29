# Implementation Steps Overview

This directory contains detailed step-by-step plans for implementing the Wspiernik backend.

## Implementation Workflow (3x3 Strategy)

1. **Before each batch:** Create/review step plan document
2. **Get approval:** Wait for user to approve the plan
3. **Implement:** Complete up to 3 tasks
4. **Review:** Present completed work for approval
5. **Repeat:** Move to next batch

---

## Step Plans

| Step | Tasks | Status | Description |
|------|-------|--------|-------------|
| [Step 01](step-01-tasks-1-3-foundation.md) | 1-3 | IN PROGRESS | Foundation & Configuration |
| [Step 02](step-02-tasks-4-5-repositories.md) | 4-5 | PENDING | Repository Layer & Data Init |
| [Step 03](step-03-tasks-6-8-llm.md) | 6-8 | PENDING | LLM Integration |
| [Step 04](step-04-tasks-9-12-websocket.md) | 9-12 | PENDING | WebSocket Infrastructure |
| [Step 05](step-05-tasks-13-15-survey.md) | 13-15 | PENDING | Survey Module |
| [Step 06](step-06-tasks-16-19-intervention.md) | 16-19 | PENDING | Intervention Module |
| [Step 07](step-07-tasks-20-22-support.md) | 20-22 | PENDING | Support Module |
| [Step 08](step-08-tasks-23-25-facts-distiller.md) | 23-25 | PENDING | Facts Distiller |
| [Step 09](step-09-tasks-26-28-logging.md) | 26-28 | PENDING | Logging & Error Handling |

---

## Progress Tracking

### Completed Tasks
- [x] Task 1: Project Configuration & Dependencies
- [x] Task 2: Application Configuration

### Current Batch (Step 01)
- [ ] Task 3: Database Entities

### Upcoming
- Task 4: Repository Layer
- Task 5: Database Initialization
- ... (see individual step files for details)

---

## Quick Reference

### Package Structure
```
com.wspiernik/
├── api/
│   ├── dto/
│   ├── rest/
│   └── websocket/
├── domain/
│   ├── survey/
│   ├── intervention/
│   ├── support/
│   ├── facts/
│   └── events/
├── infrastructure/
│   ├── llm/
│   ├── persistence/
│   ├── lifecycle/
│   └── logging/
└── utils/
```

### Key Files
- `pom.xml` - Maven dependencies
- `application.properties` - Configuration
- `WspiernikSocket.java` - WebSocket endpoint
- `MessageDispatcher.java` - Message routing
- `DataInitializer.java` - Database seeding

---

## How to Use These Plans

1. **Read the step file** before starting implementation
2. **Check acceptance criteria** to understand what "done" means
3. **Follow the file structure** specified in each task
4. **Mark checkboxes** as tasks are completed
5. **Update status** in this README
