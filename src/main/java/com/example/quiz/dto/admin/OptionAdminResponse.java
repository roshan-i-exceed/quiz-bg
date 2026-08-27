package com.example.quiz.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Admin-facing option view. Unlike the User-facing equivalent, this DTO
 * DOES expose isCorrect - this is intentional and this class must never
 * be reused by any User-facing endpoint.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionAdminResponse {

    private Long id;
    private String optionText;
    private Boolean isCorrect;
    private Integer displayOrder;
}
