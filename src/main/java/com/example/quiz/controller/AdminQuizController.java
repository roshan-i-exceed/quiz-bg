package com.example.quiz.controller;

import com.example.quiz.dto.QuestionRequest;
import com.example.quiz.dto.QuizRequest;
import com.example.quiz.dto.QuizUpdateRequest;
import com.example.quiz.dto.admin.QuestionAdminResponse;
import com.example.quiz.dto.admin.QuizAdminResponse;
import com.example.quiz.dto.admin.QuizAdminSummaryResponse;
import com.example.quiz.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * All Admin-facing quiz management endpoints. These are the only endpoints
 * that ever see or return correct-answer information.
 */
@RestController
@RequestMapping("/api/admin/quizzes")
public class AdminQuizController {

    private final QuizService quizService;

    public AdminQuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    public ResponseEntity<QuizAdminResponse> createQuiz(@Valid @RequestBody QuizRequest request) {
        QuizAdminResponse response = quizService.createQuiz(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{quizId}/questions")
    public ResponseEntity<QuestionAdminResponse> addQuestion(
            @PathVariable Long quizId,
            @Valid @RequestBody QuestionRequest request) {
        QuestionAdminResponse response = quizService.addQuestion(quizId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<QuizAdminSummaryResponse>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzesForAdmin());
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<QuizAdminResponse> getQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuizForAdmin(quizId));
    }

    @PutMapping("/{quizId}")
    public ResponseEntity<QuizAdminResponse> updateQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody QuizUpdateRequest request) {
        return ResponseEntity.ok(quizService.updateQuiz(quizId, request));
    }

    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long quizId) {
        quizService.deleteQuiz(quizId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{quizId}/publish")
    public ResponseEntity<QuizAdminResponse> publishQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.publishQuiz(quizId));
    }
}
