package com.example.quiz.dto.admin;

import com.example.quiz.entity.QuizStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Lightweight quiz view (no nested questions/options) used for the
 * "list all quizzes" Admin endpoint.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAdminSummaryResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private QuizStatus status;
    private Integer questionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
