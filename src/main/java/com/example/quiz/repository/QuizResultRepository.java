package com.example.quiz.repository;

import com.example.quiz.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    List<QuizResult> findByUserIdOrderBySubmittedAtDesc(String userId);

    List<QuizResult> findByQuizIdAndUserIdOrderBySubmittedAtDesc(Long quizId, String userId);
}
