package com.autograder.domain;

import java.util.Objects;
import java.util.Optional;

/**
 * Immutable domain verdict produced after evaluating a student submission.
 *
 * <p>Constructed exclusively through factory methods — direct record construction
 * is intentionally avoided by callers so invariants (which fields are present
 * for which verdict) are enforced in one place.
 *
 * <p>Field semantics by verdict:
 * <ul>
 *   <li>{@link Verdict#PASS} — {@code diff} and {@code compileLog} are empty.
 *   <li>{@link Verdict#FAIL} — {@code diff} is present; {@code compileLog} is empty.
 *   <li>{@link Verdict#COMPILE_ERROR} — {@code compileLog} is present; {@code diff} is empty.
 *   <li>{@link Verdict#TIMEOUT} — {@code diff} and {@code compileLog} are empty.
 *   <li>{@link Verdict#INTERNAL_ERROR} — {@code compileLog} carries the error message; {@code diff} is empty.
 * </ul>
 *
 * @param verdict    the outcome classification
 * @param diff       human-readable unified diff (present for {@code FAIL})
 * @param compileLog compiler/runtime diagnostic text (present for {@code COMPILE_ERROR} and {@code INTERNAL_ERROR})
 */
public record EvaluationResult(
        Verdict verdict,
        Optional<String> diff,
        Optional<String> compileLog
) {

    public EvaluationResult {
        Objects.requireNonNull(verdict, "verdict");
        Objects.requireNonNull(diff, "diff");
        Objects.requireNonNull(compileLog, "compileLog");

        switch (verdict) {
            case PASS, TIMEOUT -> {
                if (diff.isPresent())
                    throw new IllegalArgumentException(verdict + " must not carry a diff");
                if (compileLog.isPresent())
                    throw new IllegalArgumentException(verdict + " must not carry a compileLog");
            }
            case FAIL -> {
                if (diff.isEmpty())
                    throw new IllegalArgumentException("FAIL requires a diff");
                if (compileLog.isPresent())
                    throw new IllegalArgumentException("FAIL must not carry a compileLog");
            }
            case COMPILE_ERROR, INTERNAL_ERROR -> {
                if (compileLog.isEmpty())
                    throw new IllegalArgumentException(verdict + " requires a compileLog");
                if (diff.isPresent())
                    throw new IllegalArgumentException(verdict + " must not carry a diff");
            }
        }
    }

    /** @return a passing result with no diagnostics */
    public static EvaluationResult pass() {
        return new EvaluationResult(Verdict.PASS, Optional.empty(), Optional.empty());
    }

    /**
     * @param diff unified diff of expected vs actual output
     * @return a failing result carrying the diff
     */
    public static EvaluationResult fail(String diff) {
        Objects.requireNonNull(diff, "diff");
        return new EvaluationResult(Verdict.FAIL, Optional.of(diff), Optional.empty());
    }

    /**
     * @param compileLog raw compiler stderr
     * @return a compile-error result carrying the log
     */
    public static EvaluationResult compileError(String compileLog) {
        Objects.requireNonNull(compileLog, "compileLog");
        return new EvaluationResult(Verdict.COMPILE_ERROR, Optional.empty(), Optional.of(compileLog));
    }

    /** @return a timeout result with no diagnostics */
    public static EvaluationResult timeout() {
        return new EvaluationResult(Verdict.TIMEOUT, Optional.empty(), Optional.empty());
    }

    /**
     * @param message description of the infrastructure failure
     * @return an internal-error result carrying the message
     */
    public static EvaluationResult internalError(String message) {
        Objects.requireNonNull(message, "message");
        return new EvaluationResult(Verdict.INTERNAL_ERROR, Optional.empty(), Optional.of(message));
    }
}
