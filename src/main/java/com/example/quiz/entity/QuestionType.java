package com.example.quiz.entity;

/**
 * Type of question. Only SINGLE_CHOICE is supported today, but the field
 * exists so additional types (MULTI_CHOICE, TRUE_FALSE, etc.) can be added
 * later without changing the shape of the model.
 */
public enum QuestionType {
    SINGLE_CHOICE
}
