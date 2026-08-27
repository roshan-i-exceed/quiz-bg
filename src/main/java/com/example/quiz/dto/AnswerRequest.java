package com.example.quiz.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRequest {

    @NotNull(message = "questionId must not be null")
    private Long questionId;

    @NotNull(message = "optionId must not be null")
    private Long optionId;
}
