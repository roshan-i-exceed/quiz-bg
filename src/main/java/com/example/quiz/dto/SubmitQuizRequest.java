package com.example.quiz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * The User only ever sends questionId/optionId pairs. The server - never
 * the client - determines correctness and computes the score.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmitQuizRequest {

    @NotEmpty(message = "answers must not be empty")
    @Valid
    private List<AnswerRequest> answers;
}
