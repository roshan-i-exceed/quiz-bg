package com.example.quiz.dto.admin;

import com.example.quiz.entity.QuizStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuizImportRequest {

    private String externalId;
    private String title;
    private String description;
    private String category;
    private QuizStatus status;
    private List<QuestionImportRequest> questions;
}