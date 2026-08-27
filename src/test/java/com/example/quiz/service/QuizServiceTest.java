package com.example.quiz.service;

import com.example.quiz.dto.AnswerRequest;
import com.example.quiz.dto.OptionRequest;
import com.example.quiz.dto.QuestionRequest;
import com.example.quiz.dto.QuizRequest;
import com.example.quiz.dto.QuizResultResponse;
import com.example.quiz.dto.SubmitQuizRequest;
import com.example.quiz.dto.admin.OptionAdminResponse;
import com.example.quiz.dto.admin.QuestionAdminResponse;
import com.example.quiz.dto.admin.QuizAdminResponse;
import com.example.quiz.dto.user.QuizUserDetailResponse;
import com.example.quiz.entity.QuestionType;
import com.example.quiz.exception.BadRequestException;
import com.example.quiz.exception.ConflictException;
import com.example.quiz.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the core Admin -> Publish -> User -> Submit -> Result flow
 * end-to-end against an in-memory H2 database, using a deliberately
 * non-programming subject (History) to reinforce that the backend is
 * domain-independent.
 */
@SpringBootTest
@ActiveProfiles("test")
class QuizServiceTest {

    @Autowired
    private QuizService quizService;

    @Autowired
    private ResultService resultService;

    @Test
    void adminCanCreateQuizAndAddQuestionWithOptions() {
        QuizAdminResponse quiz = quizService.createQuiz(
                new QuizRequest("World History", "General history trivia", "History"));

        assertThat(quiz.getId()).isNotNull();
        assertThat(quiz.getStatus().name()).isEqualTo("DRAFT");

        QuestionAdminResponse question = quizService.addQuestion(quiz.getId(), new QuestionRequest(
                "In which year did World War II end?",
                QuestionType.SINGLE_CHOICE,
                List.of(
                        new OptionRequest("1943", false),
                        new OptionRequest("1945", true),
                        new OptionRequest("1950", false)
                )));

        assertThat(question.getOptions()).hasSize(3);
        assertThat(question.getOptions().stream().filter(OptionAdminResponse::getIsCorrect).count()).isEqualTo(1);
    }

    @Test
    void addingQuestionWithoutExactlyOneCorrectOptionFails() {
        QuizAdminResponse quiz = quizService.createQuiz(
                new QuizRequest("Bad Quiz", "desc", "General Knowledge"));

        QuestionRequest noCorrect = new QuestionRequest(
                "Question?", QuestionType.SINGLE_CHOICE,
                List.of(new OptionRequest("A", false), new OptionRequest("B", false)));

        assertThatThrownBy(() -> quizService.addQuestion(quiz.getId(), noCorrect))
                .isInstanceOf(BadRequestException.class);

        QuestionRequest twoCorrect = new QuestionRequest(
                "Question?", QuestionType.SINGLE_CHOICE,
                List.of(new OptionRequest("A", true), new OptionRequest("B", true)));

        assertThatThrownBy(() -> quizService.addQuestion(quiz.getId(), twoCorrect))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void draftQuizIsNotVisibleToUserAndPublishedQuizIs() {
        QuizAdminResponse quiz = quizService.createQuiz(
                new QuizRequest("Movie Night", "desc", "Movies"));
        quizService.addQuestion(quiz.getId(), new QuestionRequest(
                "Pick the right answer",
                QuestionType.SINGLE_CHOICE,
                List.of(new OptionRequest("Right", true), new OptionRequest("Wrong", false))));

        // Not published yet -> user cannot see it.
        assertThatThrownBy(() -> quizService.getPublishedQuizForUser(quiz.getId()))
                .isInstanceOf(ResourceNotFoundException.class);

        quizService.publishQuiz(quiz.getId());

        QuizUserDetailResponse userView = quizService.getPublishedQuizForUser(quiz.getId());
        assertThat(userView.getQuestions()).hasSize(1);
        // The user-facing DTO type has no isCorrect field at all - this is
        // enforced at compile time by OptionUserResponse not exposing it.
    }

    @Test
    void submitQuizScoresServerSideAndIgnoresClientTrust() {
        QuizAdminResponse quiz = quizService.createQuiz(
                new QuizRequest("General Knowledge Quick Quiz", "desc", "General Knowledge"));

        QuestionAdminResponse q1 = quizService.addQuestion(quiz.getId(), new QuestionRequest(
                "What is the capital of France?",
                QuestionType.SINGLE_CHOICE,
                List.of(
                        new OptionRequest("London", false),
                        new OptionRequest("Paris", true),
                        new OptionRequest("Berlin", false))));

        QuestionAdminResponse q2 = quizService.addQuestion(quiz.getId(), new QuestionRequest(
                "What is 2 + 2?",
                QuestionType.SINGLE_CHOICE,
                List.of(
                        new OptionRequest("3", false),
                        new OptionRequest("4", true))));

        quizService.publishQuiz(quiz.getId());

        Long correctOptionQ1 = q1.getOptions().stream()
                .filter(OptionAdminResponse::getIsCorrect).findFirst().orElseThrow().getId();
        Long wrongOptionQ2 = q2.getOptions().stream()
                .filter(o -> !o.getIsCorrect()).findFirst().orElseThrow().getId();

        SubmitQuizRequest submission = new SubmitQuizRequest(List.of(
                new AnswerRequest(q1.getId(), correctOptionQ1),
                new AnswerRequest(q2.getId(), wrongOptionQ2)
        ));

        QuizResultResponse result = resultService.submitQuiz(quiz.getId(), submission);

        assertThat(result.getTotalQuestions()).isEqualTo(2);
        assertThat(result.getCorrectAnswers()).isEqualTo(1);
        assertThat(result.getWrongAnswers()).isEqualTo(1);
        assertThat(result.getScore()).isEqualTo(1);
        assertThat(result.getPercentage()).isEqualTo(50.0);
    }

    @Test
    void submitQuizRejectsOptionFromAnotherQuestion() {
        QuizAdminResponse quiz = quizService.createQuiz(
                new QuizRequest("Mismatch Quiz", "desc", "General Knowledge"));

        QuestionAdminResponse q1 = quizService.addQuestion(quiz.getId(), new QuestionRequest(
                "Question 1", QuestionType.SINGLE_CHOICE,
                List.of(new OptionRequest("A", true), new OptionRequest("B", false))));
        QuestionAdminResponse q2 = quizService.addQuestion(quiz.getId(), new QuestionRequest(
                "Question 2", QuestionType.SINGLE_CHOICE,
                List.of(new OptionRequest("C", true), new OptionRequest("D", false))));

        quizService.publishQuiz(quiz.getId());

        Long q2OptionId = q2.getOptions().get(0).getId();

        SubmitQuizRequest submission = new SubmitQuizRequest(List.of(
                new AnswerRequest(q1.getId(), q2OptionId)
        ));

        assertThatThrownBy(() -> resultService.submitQuiz(quiz.getId(), submission))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void cannotAddQuestionsToAPublishedQuiz() {
        QuizAdminResponse quiz = quizService.createQuiz(
                new QuizRequest("Locked Quiz", "desc", "General Knowledge"));
        quizService.addQuestion(quiz.getId(), new QuestionRequest(
                "Q1", QuestionType.SINGLE_CHOICE,
                List.of(new OptionRequest("A", true), new OptionRequest("B", false))));
        quizService.publishQuiz(quiz.getId());

        QuestionRequest extra = new QuestionRequest(
                "Q2", QuestionType.SINGLE_CHOICE,
                List.of(new OptionRequest("A", true), new OptionRequest("B", false)));

        assertThatThrownBy(() -> quizService.addQuestion(quiz.getId(), extra))
                .isInstanceOf(ConflictException.class);
    }
}
