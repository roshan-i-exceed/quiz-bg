package com.example.quiz.service;

import com.example.quiz.controller.OptionResponse;
import com.example.quiz.dto.OptionRequest;
import com.example.quiz.dto.QuestionRequest;
import com.example.quiz.dto.QuizRequest;
import com.example.quiz.dto.QuizUpdateRequest;
import com.example.quiz.dto.admin.OptionAdminResponse;
import com.example.quiz.dto.admin.QuestionAdminResponse;
import com.example.quiz.dto.admin.QuizAdminResponse;
import com.example.quiz.dto.admin.QuizAdminSummaryResponse;
import com.example.quiz.dto.user.OptionUserResponse;
import com.example.quiz.dto.user.QuestionUserResponse;
import com.example.quiz.dto.user.QuizUserDetailResponse;
import com.example.quiz.dto.user.QuizUserSummaryResponse;
import com.example.quiz.entity.Option;
import com.example.quiz.entity.Question;
import com.example.quiz.entity.Quiz;
import com.example.quiz.entity.QuizStatus;
import com.example.quiz.exception.BadRequestException;
import com.example.quiz.exception.ConflictException;
import com.example.quiz.exception.ResourceNotFoundException;
import com.example.quiz.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Owns the full lifecycle of a Quiz:
 * creation, question/option management, publishing,
 * and read access for both Admin and User views.
 *
 * This class knows nothing about what any given quiz is "about" -
 * it only manipulates the generic quiz/question/option structure.
 */
@Service
public class QuizService {

    private final QuizRepository quizRepository;

    public QuizService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    // ---------------------------------------------------------------
    // Admin operations
    // ---------------------------------------------------------------

    @Transactional
    public QuizAdminResponse createQuiz(QuizRequest request) {

        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .status(QuizStatus.DRAFT)
                .build();

        Quiz saved = quizRepository.save(quiz);

        return toAdminResponse(saved);
    }

    @Transactional
    public QuestionAdminResponse addQuestion(
            Long quizId,
            QuestionRequest request) {

        Quiz quiz = getQuizOrThrow(quizId);

        if (quiz.getStatus() == QuizStatus.PUBLISHED) {
            throw new ConflictException(
                    "Cannot add questions to a published quiz. "
                            + "Set its status back to DRAFT first."
            );
        }

        long correctCount = request.getOptions()
                .stream()
                .filter(OptionRequest::getIsCorrect)
                .count();

        if (correctCount != 1) {
            throw new BadRequestException(
                    "A SINGLE_CHOICE question must have exactly "
                            + "one correct option, found " + correctCount
            );
        }

        if (request.getOptions().size() < 2) {
            throw new BadRequestException(
                    "A question must have at least two options"
            );
        }

        int nextQuestionOrder = quiz.getQuestions().size();

        Question question = Question.builder()
                .questionText(request.getQuestionText())
                .questionType(request.getQuestionType())
                .displayOrder(nextQuestionOrder)
                .build();

        int optionOrder = 0;

        for (OptionRequest optionRequest : request.getOptions()) {

            Option option = Option.builder()
                    .optionText(optionRequest.getOptionText())
                    .isCorrect(optionRequest.getIsCorrect())
                    .displayOrder(optionOrder++)
                    .build();

            question.addOption(option);
        }

        quiz.addQuestion(question);

        quizRepository.save(quiz);

        Question savedQuestion =
                quiz.getQuestions().get(quiz.getQuestions().size() - 1);

        return toQuestionAdminResponse(savedQuestion);
    }

    @Transactional(readOnly = true)
    public List<QuizAdminSummaryResponse> getAllQuizzesForAdmin() {

        return quizRepository.findAll()
                .stream()
                .map(this::toAdminSummaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuizAdminResponse getQuizForAdmin(Long quizId) {

        return toAdminResponse(
                getQuizOrThrow(quizId)
        );
    }

    @Transactional
    public QuizAdminResponse updateQuiz(
            Long quizId,
            QuizUpdateRequest request) {

        Quiz quiz = getQuizOrThrow(quizId);

        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setCategory(request.getCategory());

        if (request.getStatus() != null) {
            quiz.setStatus(request.getStatus());
        }

        Quiz saved = quizRepository.save(quiz);

        return toAdminResponse(saved);
    }

    @Transactional
    public void deleteQuiz(Long quizId) {

        Quiz quiz = getQuizOrThrow(quizId);

        // Cascade + orphanRemoval on Quiz -> Question -> Option
        // handles deleting related questions/options.
        quizRepository.delete(quiz);
    }

    @Transactional
    public QuizAdminResponse publishQuiz(Long quizId) {

        Quiz quiz = getQuizOrThrow(quizId);

        if (quiz.getQuestions().isEmpty()) {
            throw new BadRequestException(
                    "Cannot publish a quiz with no questions"
            );
        }

        quiz.setStatus(QuizStatus.PUBLISHED);

        Quiz saved = quizRepository.save(quiz);

        return toAdminResponse(saved);
    }

    // ---------------------------------------------------------------
    // User operations
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<QuizUserSummaryResponse> getPublishedQuizzes() {

        return quizRepository
                .findByStatus(QuizStatus.PUBLISHED)
                .stream()
                .map(this::toUserSummaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuizUserDetailResponse getPublishedQuizForUser(
            Long quizId) {

        Quiz quiz = getPublishedQuizOrThrow(quizId);

        return toUserDetailResponse(quiz);
    }

    /**
     * Get options for a specific question.
     *
     * The repository query fetches the quiz, questions and options
     * together so lazy-loading does not fail after the Hibernate
     * session is closed.
     *
     * Correct answers are not exposed.
     */
    @Transactional(readOnly = true)
public List<OptionResponse> getQuestionOptions(
        Long quizId,
        Long questionId) {

    Quiz quiz = quizRepository
            .findQuizWithQuestion(quizId, questionId)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Quiz or question not found"
                    ));

    if (quiz.getStatus() != QuizStatus.PUBLISHED) {
        throw new ResourceNotFoundException(
                "Quiz not found with id: " + quizId
        );
    }

    Question question = quiz.getQuestions()
            .stream()
            .filter(q -> q.getId().equals(questionId))
            .findFirst()
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Question not found with id: " + questionId
                    ));

    return question.getOptions()
            .stream()
            .map(option -> new OptionResponse(
                    option.getId(),
                    option.getOptionText(),
                    option.getDisplayOrder()
            ))
            .toList();
    }

    // ---------------------------------------------------------------
    // Shared internal lookups
    // ---------------------------------------------------------------

    Quiz getQuizOrThrow(Long quizId) {

        return quizRepository.findById(quizId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Quiz not found with id: " + quizId
                        ));
    }

    Quiz getPublishedQuizOrThrow(Long quizId) {

        Quiz quiz = getQuizOrThrow(quizId);

        if (quiz.getStatus() != QuizStatus.PUBLISHED) {
            throw new ResourceNotFoundException(
                    "Quiz not found with id: " + quizId
            );
        }

        return quiz;
    }

    // ---------------------------------------------------------------
    // Entity -> DTO mapping
    // ---------------------------------------------------------------

    private QuizAdminResponse toAdminResponse(Quiz quiz) {

        return QuizAdminResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .category(quiz.getCategory())
                .status(quiz.getStatus())
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .questions(
                        quiz.getQuestions()
                                .stream()
                                .map(this::toQuestionAdminResponse)
                                .collect(Collectors.toList())
                )
                .build();
    }

    private QuizAdminSummaryResponse toAdminSummaryResponse(
            Quiz quiz) {

        return QuizAdminSummaryResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .category(quiz.getCategory())
                .status(quiz.getStatus())
                .questionCount(quiz.getQuestions().size())
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .build();
    }

    private QuestionAdminResponse toQuestionAdminResponse(
            Question question) {

        return QuestionAdminResponse.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType())
                .displayOrder(question.getDisplayOrder())
                .options(
                        question.getOptions()
                                .stream()
                                .map(this::toOptionAdminResponse)
                                .collect(Collectors.toList())
                )
                .build();
    }

    private OptionAdminResponse toOptionAdminResponse(
            Option option) {

        return OptionAdminResponse.builder()
                .id(option.getId())
                .optionText(option.getOptionText())
                .isCorrect(option.getIsCorrect())
                .displayOrder(option.getDisplayOrder())
                .build();
    }

    private QuizUserSummaryResponse toUserSummaryResponse(
            Quiz quiz) {

        return QuizUserSummaryResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .category(quiz.getCategory())
                .questionCount(quiz.getQuestions().size())
                .build();
    }

    private QuizUserDetailResponse toUserDetailResponse(
            Quiz quiz) {

        return QuizUserDetailResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .category(quiz.getCategory())
                .questions(
                        quiz.getQuestions()
                                .stream()
                                .map(this::toQuestionUserResponse)
                                .collect(Collectors.toList())
                )
                .build();
    }

    private QuestionUserResponse toQuestionUserResponse(
            Question question) {

        return QuestionUserResponse.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType())
                .displayOrder(question.getDisplayOrder())
                .options(
                        question.getOptions()
                                .stream()
                                .map(this::toOptionUserResponse)
                                .collect(Collectors.toList())
                )
                .build();
    }

    private OptionUserResponse toOptionUserResponse(
            Option option) {

        return OptionUserResponse.builder()
                .id(option.getId())
                .optionText(option.getOptionText())
                .displayOrder(option.getDisplayOrder())
                .build();
    }
}