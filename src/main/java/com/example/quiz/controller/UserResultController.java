package com.example.quiz.controller;

import com.example.quiz.dto.QuizResultResponse;
import com.example.quiz.service.ResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Returns the current User's full result history across all quizzes.
 */
@RestController
@RequestMapping("/api/user/results")
public class UserResultController {

    private final ResultService resultService;

    public UserResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping
    public ResponseEntity<List<QuizResultResponse>> getMyResults() {
        return ResponseEntity.ok(resultService.getResultsForCurrentUser());
    }
}
