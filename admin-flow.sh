#!/usr/bin/env bash
#
# One-shot Admin flow runner - no Postman, no Newman, just curl + jq.
# Run this directly from the VS Code integrated terminal:
#
#   chmod +x admin-flow.sh
#   ./admin-flow.sh
#
# Requires: the backend running on BASE_URL, and `jq` installed
# (macOS: brew install jq | Ubuntu/Debian: sudo apt install jq | Windows: choco install jq)

set -e  # stop immediately if any step fails

BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "=================================================="
echo " 1. Create Quiz"
echo "=================================================="
CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/admin/quizzes" \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Java Basics",
        "description": "Basic Java knowledge",
        "category": "Programming"
      }')
echo "$CREATE_RESPONSE" | jq .
QUIZ_ID=$(echo "$CREATE_RESPONSE" | jq -r '.id')
echo ">> quizId = $QUIZ_ID"
echo

echo "=================================================="
echo " 2. Add Question 1"
echo "=================================================="
Q1_RESPONSE=$(curl -s -X POST "$BASE_URL/api/admin/quizzes/$QUIZ_ID/questions" \
  -H "Content-Type: application/json" \
  -d '{
        "questionText": "Which keyword is used to inherit a class in Java?",
        "questionType": "SINGLE_CHOICE",
        "options": [
          { "optionText": "implements", "isCorrect": false },
          { "optionText": "extends", "isCorrect": true },
          { "optionText": "inherits", "isCorrect": false },
          { "optionText": "super", "isCorrect": false }
        ]
      }')
echo "$Q1_RESPONSE" | jq .
QUESTION1_ID=$(echo "$Q1_RESPONSE" | jq -r '.id')
CORRECT_OPTION1_ID=$(echo "$Q1_RESPONSE" | jq -r '.options[] | select(.isCorrect==true) | .id')
echo ">> questionId = $QUESTION1_ID, correctOptionId = $CORRECT_OPTION1_ID"
echo

echo "=================================================="
echo " 3. Add Question 2"
echo "=================================================="
Q2_RESPONSE=$(curl -s -X POST "$BASE_URL/api/admin/quizzes/$QUIZ_ID/questions" \
  -H "Content-Type: application/json" \
  -d '{
        "questionText": "Which of these is not a Java primitive type?",
        "questionType": "SINGLE_CHOICE",
        "options": [
          { "optionText": "int", "isCorrect": false },
          { "optionText": "boolean", "isCorrect": false },
          { "optionText": "String", "isCorrect": true },
          { "optionText": "double", "isCorrect": false }
        ]
      }')
echo "$Q2_RESPONSE" | jq .
QUESTION2_ID=$(echo "$Q2_RESPONSE" | jq -r '.id')
CORRECT_OPTION2_ID=$(echo "$Q2_RESPONSE" | jq -r '.options[] | select(.isCorrect==true) | .id')
echo ">> questionId = $QUESTION2_ID, correctOptionId = $CORRECT_OPTION2_ID"
echo

echo "=================================================="
echo " 4. Get All Quizzes"
echo "=================================================="
curl -s "$BASE_URL/api/admin/quizzes" | jq .
echo

echo "=================================================="
echo " 5. Get Quiz By Id (should show 2 questions + answers)"
echo "=================================================="
curl -s "$BASE_URL/api/admin/quizzes/$QUIZ_ID" | jq .
echo

echo "=================================================="
echo " 6. Update Quiz"
echo "=================================================="
curl -s -X PUT "$BASE_URL/api/admin/quizzes/$QUIZ_ID" \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Java Basics (Updated)",
        "description": "Basic Java knowledge - core concepts",
        "category": "Programming"
      }' | jq .
echo

echo "=================================================="
echo " 7. Publish Quiz"
echo "=================================================="
curl -s -X PUT "$BASE_URL/api/admin/quizzes/$QUIZ_ID/publish" | jq .
echo

echo "=================================================="
echo " 8. (User side) Confirm it now shows up as available"
echo "=================================================="
curl -s "$BASE_URL/api/quizzes" | jq .
echo

echo "=================================================="
echo " 9. (User side) Submit the quiz (all correct)"
echo "=================================================="
SUBMIT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/quizzes/$QUIZ_ID/submit" \
  -H "Content-Type: application/json" \
  -d "{
        \"answers\": [
          { \"questionId\": $QUESTION1_ID, \"optionId\": $CORRECT_OPTION1_ID },
          { \"questionId\": $QUESTION2_ID, \"optionId\": $CORRECT_OPTION2_ID }
        ]
      }")
echo "$SUBMIT_RESPONSE" | jq .
echo

echo "=================================================="
echo " Done. quizId=$QUIZ_ID left PUBLISHED for further testing."
echo " To delete it: curl -X DELETE $BASE_URL/api/admin/quizzes/$QUIZ_ID"
echo "=================================================="
