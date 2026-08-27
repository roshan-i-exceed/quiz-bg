package com.example.quiz.dto;

import com.example.quiz.entity.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {

    @NotBlank(message = "questionText must not be empty")
    private String questionText;

    @NotNull(message = "questionType must not be null")
    private QuestionType questionType;

    @NotEmpty(message = "a question must have at least two options")
    @Valid
    private List<OptionRequest> options;
}
