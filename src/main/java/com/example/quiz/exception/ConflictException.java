package com.example.quiz.exception;

/**
 * Thrown when a request conflicts with the current state of a resource,
 * e.g. trying to modify/publish a quiz in a way that isn't allowed given
 * its current status. Mapped to HTTP 409.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
