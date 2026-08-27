package com.example.quiz.service;

import com.example.quiz.config.AppConstants;
import com.example.quiz.dto.AnswerRequest;
import com.example.quiz.dto.QuizResultResponse;
import com.example.quiz.dto.SubmitQuizRequest;
import com.example.quiz.entity.Option;
import com.example.quiz.entity.Question;
import com.example.quiz.entity.Quiz;
import com.example.quiz.entity.QuizResult;
import com.example.quiz.exception.BadRequestException;
import com.example.quiz.repository.QuizResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Handles quiz submission scoring and result retrieval.
 *
 * The scoring logic is the single most security-sensitive part of this
 * application: the correct answer is only ever compared against what is
 * stored server-side. The client's submitted score (if any) is ignored -
 * in fact the client is never even given the option to submit one.
 */
@Service
public class ResultService {

    private final QuizService quizService;
    private final QuizResultRepository quizResultRepository;

    public ResultService(QuizService quizService, QuizResultRepository quizResultRepository) {
        this.quizService = quizService;
        this.quizResultRepository = quizResultRepository;
    }

    @Transactional
    public QuizResultResponse submitQuiz(Long quizId, SubmitQuizRequest request) {
        Quiz quiz = quizService.getPublishedQuizOrThrow(quizId);

        Map<Long, Question> questionsById = quiz.getQuestions().stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        // Reject duplicate answers for the same question - the client
        // must submit exactly one answer per question it wants scored.
        Set<Long> seenQuestionIds = new HashSet<>();

        int correctAnswers = 0;

        for (AnswerRequest answer : request.getAnswers()) {
            Question question = questionsById.get(answer.getQuestionId());
            if (question == null) {
                throw new BadRequestException(
                        "Question " + answer.getQuestionId() + " does not belong to quiz " + quizId);
            }
            if (!seenQuestionIds.add(question.getId())) {
                throw new BadRequestException(
                        "Duplicate answer submitted for question " + question.getId());
            }

            Option selectedOption = question.getOptions().stream()
                    .filter(option -> option.getId().equals(answer.getOptionId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException(
                            "Option " + answer.getOptionId() + " does not belong to question " + question.getId()));

            if (Boolean.TRUE.equals(selectedOption.getIsCorrect())) {
                correctAnswers++;
            }
        }

        int totalQuestions = quiz.getQuestions().size();
        int wrongAnswers = totalQuestions - correctAnswers;
        double percentage = totalQuestions == 0
                ? 0.0
                : Math.round((correctAnswers * 10000.0) / totalQuestions) / 100.0;

        QuizResult result = QuizResult.builder()
                .quiz(quiz)
                .userId(AppConstants.DEFAULT_USER_ID)
                .score(correctAnswers)
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .wrongAnswers(wrongAnswers)
                .percentage(percentage)
                .build();

        QuizResult saved = quizResultRepository.save(result);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<QuizResultResponse> getResultsForCurrentUser() {
        return quizResultRepository.findByUserIdOrderBySubmittedAtDesc(AppConstants.DEFAULT_USER_ID).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuizResultResponse> getResultsForQuiz(Long quizId) {
        return quizResultRepository
                .findByQuizIdAndUserIdOrderBySubmittedAtDesc(quizId, AppConstants.DEFAULT_USER_ID).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private QuizResultResponse toResponse(QuizResult result) {
        return QuizResultResponse.builder()
                .id(result.getId())
                .quizId(result.getQuiz().getId())
                .quizTitle(result.getQuiz().getTitle())
                .score(result.getScore())
                .totalQuestions(result.getTotalQuestions())
                .correctAnswers(result.getCorrectAnswers())
                .wrongAnswers(result.getWrongAnswers())
                .percentage(result.getPercentage())
                .submittedAt(result.getSubmittedAt())
                .build();
    }
}
