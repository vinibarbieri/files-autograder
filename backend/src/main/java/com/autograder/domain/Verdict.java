package com.autograder.domain;

/**
 * The possible outcomes of evaluating a student submission.
 *
 * <p>Used by {@link EvaluationResult} and surfaced to the web layer
 * as the top-level status in the JSON response.
 */
public enum Verdict {
    PASS,
    FAIL,
    COMPILE_ERROR,
    TIMEOUT,
    INTERNAL_ERROR
}
