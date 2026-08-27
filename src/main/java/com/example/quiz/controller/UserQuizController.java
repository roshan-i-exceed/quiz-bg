package com.example.quiz.controller;

import com.example.quiz.dto.QuizResultResponse;
import com.example.quiz.dto.SubmitQuizRequest;
import com.example.quiz.dto.user.QuizUserDetailResponse;
import com.example.quiz.dto.user.QuizUserSummaryResponse;
import com.example.quiz.service.QuizService;
import com.example.quiz.service.ResultService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User-facing endpoints for browsing and taking published quizzes.
 *
 * Correct answers (isCorrect) are never exposed to the user.
 */
@RestController
@RequestMapping("/api/quizzes")
public class UserQuizController {

    private final QuizService quizService;
    private final ResultService resultService;

    public UserQuizController(
            QuizService quizService,
            ResultService resultService) {

        this.quizService = quizService;
        this.resultService = resultService;
    }

    /**
     * Get all published quizzes.
     *
     * GET /api/quizzes
     */
    @GetMapping
    public ResponseEntity<List<QuizUserSummaryResponse>> getAvailableQuizzes() {

        return ResponseEntity.ok(
                quizService.getPublishedQuizzes()
        );
    }

    /**
     * Get a published quiz with its questions and options.
     *
     * GET /api/quizzes/{quizId}
     */
    @GetMapping("/{quizId}")
    public ResponseEntity<QuizUserDetailResponse> getQuiz(
            @PathVariable Long quizId) {

        return ResponseEntity.ok(
                quizService.getPublishedQuizForUser(quizId)
        );
    }

    /**
     * Get options for a specific question.
     *
     * GET /api/quizzes/{quizId}/questions/{questionId}/options
     *
     * Correct answers are never exposed.
     */
    @GetMapping("/{quizId}/questions/{questionId}/options")
    public ResponseEntity<List<OptionResponse>> getQuestionOptions(
            @PathVariable Long quizId,
            @PathVariable Long questionId) {

        return ResponseEntity.ok(
                quizService.getQuestionOptions(
                        quizId,
                        questionId
                )
        );
    }

    /**
     * Submit a completed quiz.
     *
     * POST /api/quizzes/{quizId}/submit
     */
    @PostMapping("/{quizId}/submit")
    public ResponseEntity<QuizResultResponse> submitQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody SubmitQuizRequest request) {

        QuizResultResponse response =
                resultService.submitQuiz(quizId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Get results for a specific quiz.
     *
     * GET /api/quizzes/{quizId}/results
     */
    @GetMapping("/{quizId}/results")
    public ResponseEntity<List<QuizResultResponse>> getResultsForQuiz(
            @PathVariable Long quizId) {

        return ResponseEntity.ok(
                resultService.getResultsForQuiz(quizId)
        );
    }
}