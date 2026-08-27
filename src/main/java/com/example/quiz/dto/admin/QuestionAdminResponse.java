package com.example.quiz.dto.admin;

import com.example.quiz.entity.QuestionType;
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
public class QuestionAdminResponse {

    private Long id;
    private String questionText;
    private QuestionType questionType;
    private Integer displayOrder;
    private List<OptionAdminResponse> options;
}
