package com.example.quiz.dto.admin;

import com.example.quiz.entity.QuizStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAdminResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private QuizStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<QuestionAdminResponse> questions;
}
