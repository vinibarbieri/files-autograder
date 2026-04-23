package com.autograder.domain;

/**
 * Thrown when a {@link Submission} fails validation before any execution is
 * attempted. Carries a stable {@link Reason} so the web layer can translate it
 * to a precise HTTP status and problem-detail body without string matching.
 */
public class InvalidSubmissionException extends RuntimeException {

    public enum Reason {
        EMPTY_SOURCE,
        SOURCE_TOO_LARGE,
        DISALLOWED_EXTENSION,
        INVALID_ASSIGNMENT_ID
    }

    private final Reason reason;

    public InvalidSubmissionException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}
