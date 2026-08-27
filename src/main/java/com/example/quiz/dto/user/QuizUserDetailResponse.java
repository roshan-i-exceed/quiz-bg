package com.example.quiz.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizUserDetailResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private List<QuestionUserResponse> questions;
}
