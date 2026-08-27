package com.example.quiz.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizUserSummaryResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private Integer questionCount;
}
