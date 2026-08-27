# Quiz Backend

A simple, clean, **domain-independent** backend for a Quiz Application.

The backend has no idea what a quiz is *about*. It only understands the
generic structure of a quiz: `Quiz -> Question -> Option`, plus scoring and
results. A quiz can be about Java, history, movies, cricket, company
onboarding, or anything else — the Admin decides that through data, not
through code.

> **Core principle:** the backend understands what a quiz *is*, not what a
> quiz is *about*.

---

## 1. Project Overview

- **1 Admin** creates quizzes, questions, and options (via Postman — no
  Admin UI), and publishes quizzes.
- **1 User** browses published quizzes, answers them, and gets scored
  results.
- Correct answers (`isCorrect`) are stored and evaluated **only on the
  server**. The client is never trusted to compute or report its own score.
- Authentication is intentionally minimal — there is exactly one Admin and
  one User, modeled as fixed identities rather than a full auth system.

## 2. Architecture

Clean, layered, modular monolith:

```
controller  →  service  →  repository  →  database
     ↓
    dto  (entities never leave the service layer)
```

- `controller` — thin, HTTP-only concerns
- `service` — all business logic (validation, scoring, publish rules)
- `repository` — Spring Data JPA, persistence only
- `entity` — JPA entities, generic quiz model
- `dto` — request/response shapes; **admin DTOs** (`dto/admin`) may expose
  `isCorrect`, **user DTOs** (`dto/user`) never do
- `exception` — a global exception handler mapping to proper HTTP codes

## 3. Tech Stack

- Java 17
- Spring Boot 3.3 (Web, Data JPA, Validation)
- PostgreSQL
- Maven
- Lombok
- JUnit 5 + AssertJ (via `spring-boot-starter-test`)
- H2 (test-only, so `mvn test` doesn't require a running Postgres instance)

## 4. Project Structure

```
src/main/java/com/example/quiz
├── controller
│   ├── AdminQuizController      (Admin CRUD + publish)
│   ├── UserQuizController       (browse / answer / submit)
│   └── UserResultController     (result history)
├── service
│   ├── QuizService               (quiz/question/option lifecycle)
│   └── ResultService              (server-side scoring, results)
├── repository
│   ├── QuizRepository
│   └── QuizResultRepository
├── entity
│   ├── Quiz, Question, Option, QuizResult
│   └── QuizStatus, QuestionType (enums)
├── dto
│   ├── QuizRequest, QuizUpdateRequest, QuestionRequest, OptionRequest
│   ├── SubmitQuizRequest, AnswerRequest, QuizResultResponse
│   ├── admin/  (QuizAdminResponse, QuestionAdminResponse, OptionAdminResponse, QuizAdminSummaryResponse)
│   └── user/   (QuizUserDetailResponse, QuestionUserResponse, OptionUserResponse, QuizUserSummaryResponse)
├── exception
│   ├── GlobalExceptionHandler
│   ├── ResourceNotFoundException (404)
│   ├── BadRequestException (400)
│   └── ConflictException (409)
├── config
│   ├── AppConstants   (single fixed user id — see note below)
│   └── DataSeeder     (seeds demo data on startup)
└── QuizApplication
```

## 5. Database Model

**Quiz**: `id, title, description, category, status, createdAt, updatedAt`
**Question**: `id, quizId, questionText, questionType, displayOrder`
**Option**: `id, questionId, optionText, isCorrect, displayOrder`
**QuizResult**: `id, quizId, userId, score, totalQuestions, correctAnswers, wrongAnswers, percentage, submittedAt`

Relationships:

```
Quiz (1) ── (*) Question (1) ── (*) Option
Quiz (1) ── (*) QuizResult
```

`category` is a free-text field. The backend never validates or branches on
its value — there are no hardcoded subjects anywhere in the codebase.

`status` is `DRAFT` or `PUBLISHED`. Only `PUBLISHED` quizzes are visible to
the User. Adding questions to an already-published quiz is rejected (409) —
set it back to `DRAFT` via the update endpoint first if you need to edit it.

**Note on the single user:** since this project has exactly one User and
explicitly avoids building real auth, `QuizResult.userId` is set to a fixed
constant (`AppConstants.DEFAULT_USER_ID`). It's a real column (not
hardcoded logic scattered around), so wiring in real multi-user auth later
means replacing one lookup, not redesigning the model.

## 6. API List

### Admin APIs (`/api/admin/quizzes`)

| Method | Path | Description |
|---|---|---|
| POST | `/api/admin/quizzes` | Create a quiz (starts as `DRAFT`) |
| POST | `/api/admin/quizzes/{quizId}/questions` | Add a question + options to a quiz |
| GET | `/api/admin/quizzes` | List all quizzes (draft + published) |
| GET | `/api/admin/quizzes/{quizId}` | Get full quiz incl. questions/options **and correct answers** |
| PUT | `/api/admin/quizzes/{quizId}` | Update title/description/category/status |
| DELETE | `/api/admin/quizzes/{quizId}` | Delete a quiz (cascades to questions/options) |
| PUT | `/api/admin/quizzes/{quizId}/publish` | Publish a quiz (requires ≥1 question) |

### User APIs

| Method | Path | Description |
|---|---|---|
| GET | `/api/quizzes` | List published quizzes only |
| GET | `/api/quizzes/{quizId}` | Get a published quiz's questions/options — **no correct answers** |
| POST | `/api/quizzes/{quizId}/submit` | Submit answers; server computes and stores the score |
| GET | `/api/quizzes/{quizId}/results` | Result history for this quiz |
| GET | `/api/user/results` | Full result history across all quizzes |

## 7. Admin Flow

1. `POST /api/admin/quizzes` — create the quiz shell (`DRAFT`)
2. `POST /api/admin/quizzes/{quizId}/questions` — add one or more questions,
   each with ≥2 options and **exactly one** `isCorrect: true`
3. `GET /api/admin/quizzes/{quizId}` — review the quiz, including answers
4. `PUT /api/admin/quizzes/{quizId}/publish` — publish it

## 8. User Flow

1. `GET /api/quizzes` — see what's published
2. `GET /api/quizzes/{quizId}` — fetch questions/options (no answers)
3. `POST /api/quizzes/{quizId}/submit` — send `{questionId, optionId}` pairs
4. Read the returned score, or fetch it again later via
   `GET /api/user/results`

## 9. Security Rule: the server always grades

The User never sends a score. The submit request only contains
`questionId`/`optionId` pairs:

```json
{
  "answers": [
    { "questionId": 101, "optionId": 1001 }
  ]
}
```

The server:

1. Confirms the quiz exists and is `PUBLISHED`.
2. Confirms every submitted `questionId` belongs to that quiz.
3. Confirms every submitted `optionId` belongs to that question.
4. Rejects duplicate answers for the same question.
5. Compares the selected option against the stored `isCorrect` flag.
6. Computes `correctAnswers`, `wrongAnswers`, `score`, `percentage`.
7. Persists a `QuizResult` and returns it.

`isCorrect` is **never** present in any User-facing response — this is
enforced by using a completely separate `OptionUserResponse` DTO that has
no such field, rather than filtering a shared DTO.

## 10. Validation & Error Handling

| Rule | Status |
|---|---|
| Quiz/question/option not found | 404 |
| Missing/blank title, empty options, wrong number of correct options, unknown question/option in a submission | 400 |
| Adding questions to a published quiz, publishing a quiz with no questions | 409 |
| Successful creation | 201 |
| Successful read/update | 200 |
| Successful delete | 204 |

All errors return a consistent JSON body:

```json
{
  "timestamp": "2026-08-26T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "A SINGLE_CHOICE question must have exactly one correct option, found 0"
}
```

## 11. How to Run

### Prerequisites

- JDK 17+
- Maven 3.8+
- A running PostgreSQL instance

### Database Setup

Create a database (default expected name is `quizdb`, but this is fully
configurable):

```bash
createdb quizdb
```

### Environment Variables

Copy `.env.example` to `.env` (or export these directly) — nothing is
hardcoded:

```
DB_URL=jdbc:postgresql://localhost:5432/quizdb
DB_USERNAME=postgres
DB_PASSWORD=postgres
SERVER_PORT=8080
DDL_AUTO=update
SHOW_SQL=false
```

### Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

The app starts on `http://localhost:8080` and seeds demo data on first run
(see below) — no manual setup required to start testing immediately.

### Run Tests

```bash
mvn test
```

Tests run against an in-memory H2 database (`src/test/resources/application-test.properties`),
so no Postgres instance is needed just to run `mvn test`.

## 12. Seed Data

On first startup (only if the database is empty), the app seeds:

- **1 published quiz** — "Classic Movies Trivia" (category: `Movies`), with
  3 questions, ready to be fetched and submitted by the User immediately.
- **1 draft quiz** — "Cricket World Cup Basics" (category: `Cricket`), which
  will **not** appear in `GET /api/quizzes` until an Admin publishes it —
  demonstrating the draft/published visibility rule.

Using a movies/cricket quiz (rather than a programming one) in the seed
data is deliberate: it shows the backend doesn't secretly favor any
subject.

## 13. Postman Testing

A ready-to-import collection is included: `postman_collection.json`.

Import it into Postman, set the `baseUrl` variable (defaults to
`http://localhost:8080`), and run requests top to bottom. After creating a
quiz/question, copy the returned `id` values into the `quizId`,
`questionId`, and `optionId` collection variables to chain the later
requests (publish, fetch, submit).

Manual sequence (matches the collection):

1. `POST /api/admin/quizzes`
2. `POST /api/admin/quizzes/{quizId}/questions`
3. `PUT /api/admin/quizzes/{quizId}/publish`
4. `GET /api/quizzes`
5. `GET /api/quizzes/{quizId}`
6. `POST /api/quizzes/{quizId}/submit`
7. `GET /api/user/results`

### Sample: Create Quiz

Request:
```json
POST /api/admin/quizzes
{
  "title": "Java Basics",
  "description": "Basic Java knowledge",
  "category": "Programming"
}
```

Response `201`:
```json
{
  "id": 1,
  "title": "Java Basics",
  "description": "Basic Java knowledge",
  "category": "Programming",
  "status": "DRAFT",
  "createdAt": "2026-08-26T10:00:00",
  "updatedAt": "2026-08-26T10:00:00",
  "questions": []
}
```

### Sample: Submit Quiz

Request:
```json
POST /api/quizzes/1/submit
{
  "answers": [
    { "questionId": 101, "optionId": 1002 }
  ]
}
```

Response `201`:
```json
{
  "id": 1,
  "quizId": 1,
  "quizTitle": "Java Basics",
  "score": 1,
  "totalQuestions": 1,
  "correctAnswers": 1,
  "wrongAnswers": 0,
  "percentage": 100.0,
  "submittedAt": "2026-08-26T10:05:00"
}
```

## 14. Future Extension Points

The model is intentionally minimal so these can be layered on without a
rewrite:

- Real multi-user support (swap `AppConstants.DEFAULT_USER_ID` for an
  authenticated principal)
- Admin authentication, multiple admins
- Timed quizzes
- Additional question types (multi-select, true/false) — `questionType`
  already exists as an enum for this
- Multiple correct answers per question
- Quiz attempt limits / retakes tracking
- Tags, difficulty, explanations per question
- Randomized question/option order
- Pagination on list endpoints
- Analytics, leaderboards

None of these require touching the core `Quiz -> Question -> Option`
structure — they extend it.

## 15. Code Quality Notes

- Constructor injection everywhere, no field injection.
- Controllers are thin; all business rules live in services.
- Entities never cross the controller boundary — only DTOs do.
- Admin and User read models are deliberately separate types
  (`dto/admin` vs `dto/user`), so "never leak `isCorrect`" is a compile-time
  guarantee for the User path, not a runtime filtering step that could be
  forgotten later.
#   q u i z - b g  
 