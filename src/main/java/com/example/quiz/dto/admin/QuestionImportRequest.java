package com.example.quiz.dto.admin;

import com.example.quiz.entity.QuestionType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionImportRequest {

    private String externalId;
    private String questionText;
    private QuestionType questionType;
    private Integer points;
    private List<OptionImportRequest> options;
}