package com.example.quiz.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OptionImportRequest {

    private String optionText;
    private Boolean isCorrect;
}