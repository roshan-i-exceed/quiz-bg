package com.example.quiz.service;

import com.example.quiz.dto.admin.OptionImportRequest;
import com.example.quiz.dto.admin.QuestionImportRequest;
import com.example.quiz.dto.admin.QuizImportRequest;
import com.example.quiz.entity.Option;
import com.example.quiz.entity.Question;
import com.example.quiz.entity.Quiz;
import com.example.quiz.entity.QuizStatus;
import com.example.quiz.exception.BadRequestException;
import com.example.quiz.repository.QuizRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;

@Service
public class QuizImportService {

    private final QuizRepository quizRepository;
    private final ObjectMapper objectMapper;

    public QuizImportService(
            QuizRepository quizRepository,
            ObjectMapper objectMapper) {
        this.quizRepository = quizRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Quiz importQuiz(String filePath) {

        QuizImportRequest request;

        try {
            request = objectMapper.readValue(
                    new File(filePath),
                    QuizImportRequest.class
            );
        } catch (IOException e) {
            throw new BadRequestException(
                    "Unable to read quiz JSON file: " + filePath
            );
        }

        validate(request);

        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .status(QuizStatus.DRAFT)
                .build();

        int questionOrder = 0;

        for (QuestionImportRequest questionRequest : request.getQuestions()) {

            Question question = Question.builder()
                    .questionText(questionRequest.getQuestionText())
                    .questionType(questionRequest.getQuestionType())
                    .displayOrder(questionOrder++)
                    .build();

            int optionOrder = 0;

            for (OptionImportRequest optionRequest : questionRequest.getOptions()) {

                Option option = Option.builder()
                        .optionText(optionRequest.getOptionText())
                        .isCorrect(optionRequest.getIsCorrect())
                        .displayOrder(optionOrder++)
                        .build();

                question.addOption(option);
            }

            quiz.addQuestion(question);
        }

        if (request.getStatus() != null) {
            quiz.setStatus(request.getStatus());
        }

        Quiz savedQuiz = quizRepository.save(quiz);

        System.out.println(
                "Quiz imported successfully: "
                        + savedQuiz.getTitle()
                        + " | Questions: "
                        + savedQuiz.getQuestions().size()
                        + " | Status: "
                        + savedQuiz.getStatus()
        );

        return savedQuiz;
    }

    private void validate(QuizImportRequest request) {

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new BadRequestException("Quiz title is required");
        }

        if (request.getCategory() == null || request.getCategory().isBlank()) {
            throw new BadRequestException("Quiz category is required");
        }

        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new BadRequestException(
                    "Quiz must contain at least one question"
            );
        }

        for (QuestionImportRequest question : request.getQuestions()) {

            if (question.getQuestionText() == null
                    || question.getQuestionText().isBlank()) {
                throw new BadRequestException(
                        "Question text cannot be empty"
                );
            }

            if (question.getOptions() == null
                    || question.getOptions().size() < 2) {
                throw new BadRequestException(
                        "Every question must have at least two options"
                );
            }

            long correctAnswers = question.getOptions()
                    .stream()
                    .filter(option ->
                            Boolean.TRUE.equals(option.getIsCorrect()))
                    .count();

            if (correctAnswers != 1) {
                throw new BadRequestException(
                        "Every SINGLE_CHOICE question must have exactly one correct option"
                );
            }
        }
    }
}