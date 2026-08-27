package com.example.quiz.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Generic quiz creation/update payload. "category" is a free-text label -
 * the backend never validates it against a fixed list of subjects.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizRequest {

    @NotBlank(message = "title must not be empty")
    private String title;

    private String description;

    @NotBlank(message = "category must not be empty")
    private String category;
}
