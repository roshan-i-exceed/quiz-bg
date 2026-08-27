package com.example.quiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OptionRequest {

    @NotBlank(message = "optionText must not be empty")
    private String optionText;

    @NotNull(message = "isCorrect must be provided")
    private Boolean isCorrect;
}
