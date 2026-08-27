package com.example.quiz.repository;

import com.example.quiz.entity.Quiz;
import com.example.quiz.entity.QuizStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByStatus(QuizStatus status);

    @Query("""
        SELECT DISTINCT q
        FROM Quiz q
        JOIN FETCH q.questions question
        WHERE q.id = :quizId
          AND question.id = :questionId
    """)
    Optional<Quiz> findQuizWithQuestion(
            @Param("quizId") Long quizId,
            @Param("questionId") Long questionId
    );
}