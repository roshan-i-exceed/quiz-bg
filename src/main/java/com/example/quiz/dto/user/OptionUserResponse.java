package com.example.quiz.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User-facing option view. Deliberately has NO isCorrect field - this is
 * the safeguard that prevents the correct answer from ever leaking to the
 * client taking the quiz.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionUserResponse {

    private Long id;
    private String optionText;
    private Integer displayOrder;
}
