package com.example.quiz.config;

/**
 * This project intentionally supports exactly one User (per the spec) and
 * keeps authentication out of scope. All submissions/results are recorded
 * against this fixed identifier. Swapping this for a real authenticated
 * principal later is a small, isolated change - only the code that reads
 * "current user" needs to change, not the Quiz/Question/Option model.
 */
public final class AppConstants {

    public static final String DEFAULT_USER_ID = "default-user";

    private AppConstants() {
    }
}
