package com.example.quiz.dto;

import com.example.quiz.entity.QuizStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload for PUT /api/admin/quizzes/{quizId}. Status is optional here -
 * if omitted, the current status is left unchanged. Use the dedicated
 * /publish endpoint to move a quiz from DRAFT to PUBLISHED.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizUpdateRequest {

    @NotBlank(message = "title must not be empty")
    private String title;

    private String description;

    @NotBlank(message = "category must not be empty")
    private String category;

    private QuizStatus status;
}
