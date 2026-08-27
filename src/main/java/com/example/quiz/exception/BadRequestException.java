package com.example.quiz.exception;

/**
 * Thrown when a request is structurally invalid, e.g. a question/option
 * mismatch, a missing correct option, or an attempt to submit an
 * unpublished quiz. Mapped to HTTP 400.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
