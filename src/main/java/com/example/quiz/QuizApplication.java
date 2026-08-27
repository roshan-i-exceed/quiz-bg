package com.example.quiz;

import com.example.quiz.service.QuizImportService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class QuizApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizApplication.class, args);
    }

    @Bean
    CommandLineRunner quizImportRunner(
            QuizImportService quizImportService) {

        return args -> {

            for (String arg : args) {

                if (arg.startsWith("--quiz.import=")) {

                    String filePath =
                            arg.substring("--quiz.import=".length());

                    System.out.println(
                            "Starting quiz import: " + filePath
                    );

                    quizImportService.importQuiz(filePath);

                    System.out.println(
                            "Quiz import completed."
                    );
                }
            }
        };
    }
}