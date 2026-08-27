# Quiz Backend — API Testing Guide

Base URL: `http://localhost:8080`

Test in this order — each step's response gives you an `id` you'll need
for the next step.

---

## 1. Admin — Create Quiz

```http
POST /api/admin/quizzes
Content-Type: application/json
```

**Request**
```json
{
  "title": "Java Basics",
  "description": "Basic Java knowledge",
  "category": "Programming"
}
```

**Response `201 Created`**
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
👉 Save `id: 1` as `quizId`.

---

## 2. Admin — Add Question

```http
POST /api/admin/quizzes/1/questions
Content-Type: application/json
```

**Request**
```json
{
  "questionText": "Which keyword is used to inherit a class in Java?",
  "questionType": "SINGLE_CHOICE",
  "options": [
    { "optionText": "implements", "isCorrect": false },
    { "optionText": "extends", "isCorrect": true },
    { "optionText": "inherits", "isCorrect": false },
    { "optionText": "super", "isCorrect": false }
  ]
}
```

**Response `201 Created`**
```json
{
  "id": 101,
  "questionText": "Which keyword is used to inherit a class in Java?",
  "questionType": "SINGLE_CHOICE",
  "displayOrder": 0,
  "options": [
    { "id": 1001, "optionText": "implements", "isCorrect": false, "displayOrder": 0 },
    { "id": 1002, "optionText": "extends", "isCorrect": true, "displayOrder": 1 },
    { "id": 1003, "optionText": "inherits", "isCorrect": false, "displayOrder": 2 },
    { "id": 1004, "optionText": "super", "isCorrect": false, "displayOrder": 3 }
  ]
}
```
👉 Save `id: 101` as `questionId`, and `id: 1002` (the correct option) as `optionId`.

Add a second question the same way if you want a multi-question quiz.

**Error case — wrong number of correct options** (`400 Bad Request`)
```json
{
  "timestamp": "2026-08-26T10:01:00",
  "status": 400,
  "error": "Bad Request",
  "message": "A SINGLE_CHOICE question must have exactly one correct option, found 0"
}
```

---

## 3. Admin — Get All Quizzes

```http
GET /api/admin/quizzes
```

**Response `200 OK`**
```json
[
  {
    "id": 1,
    "title": "Java Basics",
    "description": "Basic Java knowledge",
    "category": "Programming",
    "status": "DRAFT",
    "questionCount": 1,
    "createdAt": "2026-08-26T10:00:00",
    "updatedAt": "2026-08-26T10:01:00"
  }
]
```

---

## 4. Admin — Get Quiz By Id (full detail, includes answers)

```http
GET /api/admin/quizzes/1
```

**Response `200 OK`** — same shape as step 1's response but with the
`questions` array populated (each option includes `isCorrect`).

**Error case — not found** (`404 Not Found`)
```json
{
  "timestamp": "2026-08-26T10:02:00",
  "status": 404,
  "error": "Not Found",
  "message": "Quiz not found with id: 999"
}
```

---

## 5. Admin — Update Quiz

```http
PUT /api/admin/quizzes/1
Content-Type: application/json
```

**Request**
```json
{
  "title": "Java Basics (Updated)",
  "description": "Basic Java knowledge - core concepts",
  "category": "Programming"
}
```
> `status` is optional in this payload — omit it to leave status unchanged,
> or include `"status": "DRAFT"` to move a published quiz back to draft
> (needed before you can add more questions to it).

**Response `200 OK`** — updated quiz object, same shape as step 4.

---

## 6. Admin — Publish Quiz

```http
PUT /api/admin/quizzes/1/publish
```

**Response `200 OK`**
```json
{
  "id": 1,
  "title": "Java Basics (Updated)",
  "description": "Basic Java knowledge - core concepts",
  "category": "Programming",
  "status": "PUBLISHED",
  "createdAt": "2026-08-26T10:00:00",
  "updatedAt": "2026-08-26T10:03:00",
  "questions": [ /* ... */ ]
}
```

**Error case — publishing an empty quiz** (`400 Bad Request`)
```json
{
  "timestamp": "2026-08-26T10:03:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot publish a quiz with no questions"
}
```

---

## 7. User — Get Available Quizzes (published only)

```http
GET /api/quizzes
```

**Response `200 OK`**
```json
[
  {
    "id": 1,
    "title": "Java Basics (Updated)",
    "description": "Basic Java knowledge - core concepts",
    "category": "Programming",
    "questionCount": 1
  }
]
```
Note there's no `status` and no answers here — draft quizzes never appear.

---

## 8. User — Get Quiz (no correct answers exposed)

```http
GET /api/quizzes/1
```

**Response `200 OK`**
```json
{
  "id": 1,
  "title": "Java Basics (Updated)",
  "description": "Basic Java knowledge - core concepts",
  "category": "Programming",
  "questions": [
    {
      "id": 101,
      "questionText": "Which keyword is used to inherit a class in Java?",
      "questionType": "SINGLE_CHOICE",
      "displayOrder": 0,
      "options": [
        { "id": 1001, "optionText": "implements", "displayOrder": 0 },
        { "id": 1002, "optionText": "extends", "displayOrder": 1 },
        { "id": 1003, "optionText": "inherits", "displayOrder": 2 },
        { "id": 1004, "optionText": "super", "displayOrder": 3 }
      ]
    }
  ]
}
```
Note: **no `isCorrect` field anywhere** in this response.

**Error case — draft quiz, or bad id** (`404 Not Found`) — same shape as step 4's 404.

---

## 9. User — Submit Quiz

```http
POST /api/quizzes/1/submit
Content-Type: application/json
```

**Request**
```json
{
  "answers": [
    { "questionId": 101, "optionId": 1002 }
  ]
}
```

**Response `201 Created`**
```json
{
  "id": 1,
  "quizId": 1,
  "quizTitle": "Java Basics (Updated)",
  "score": 1,
  "totalQuestions": 1,
  "correctAnswers": 1,
  "wrongAnswers": 0,
  "percentage": 100.0,
  "submittedAt": "2026-08-26T10:05:00"
}
```

Try submitting a wrong option (e.g. `1001`) to see `correctAnswers: 0,
percentage: 0.0`.

**Error case — option belongs to a different question** (`400 Bad Request`)
```json
{
  "timestamp": "2026-08-26T10:06:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Option 2001 does not belong to question 101"
}
```

**Error case — duplicate answer for same question** (`400 Bad Request`)
```json
{
  "timestamp": "2026-08-26T10:06:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Duplicate answer submitted for question 101"
}
```

---

## 10. User — Get Results For This Quiz

```http
GET /api/quizzes/1/results
```

**Response `200 OK`**
```json
[
  {
    "id": 1,
    "quizId": 1,
    "quizTitle": "Java Basics (Updated)",
    "score": 1,
    "totalQuestions": 1,
    "correctAnswers": 1,
    "wrongAnswers": 0,
    "percentage": 100.0,
    "submittedAt": "2026-08-26T10:05:00"
  }
]
```
(Most recent submission first.)

---

## 11. User — Get All My Results (across every quiz)

```http
GET /api/user/results
```

**Response `200 OK`** — same array shape as step 10, but includes results
from every quiz the user has ever submitted.

---

## 12. Admin — Delete Quiz

```http
DELETE /api/admin/quizzes/1
```

**Response `204 No Content`** (empty body). Deletes the quiz and cascades
to its questions and options.

---

## Extra error cases worth trying

**Adding a question to a published quiz** (`409 Conflict`)
```http
POST /api/admin/quizzes/1/questions
```
```json
{
  "timestamp": "2026-08-26T10:07:00",
  "status": 409,
  "error": "Conflict",
  "message": "Cannot add questions to a published quiz. Set its status back to DRAFT first."
}
```

**Blank title on create** (`400 Bad Request`)
```json
{ "title": "", "description": "x", "category": "x" }
```
```json
{
  "timestamp": "2026-08-26T10:08:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: title - title must not be empty; "
}
```

---

## Ready-to-import option

`postman_collection.json` (included in the project zip) has all of these
requests pre-built with `{{baseUrl}}`, `{{quizId}}`, `{{questionId}}`, and
`{{optionId}}` variables — just fill in the variables as you go and run the
requests top to bottom.
