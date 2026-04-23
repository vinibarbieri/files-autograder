package com.autograder.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * A single student attempt at an assignment.
 *
 * <p>Immutable value object. The domain never mutates a submission once
 * constructed; subsequent processing produces a new object
 * ({@link EvaluationResult}).
 *
 * @param assignmentId identifier of the assignment being attempted
 * @param fileName original uploaded file name (for diagnostics only)
 * @param source raw bytes of the student's C source file
 * @param clientIp originating IP; optional, used for edge rate-limiting later
 * @param submittedAt server-side receipt timestamp
 */
public record Submission(
        String assignmentId,
        String fileName,
        byte[] source,
        String clientIp,
        Instant submittedAt
) {

    public Submission {
        Objects.requireNonNull(assignmentId, "assignmentId");
        Objects.requireNonNull(fileName, "fileName");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(submittedAt, "submittedAt");
    }

    /**
     * @return the uploaded source size in bytes
     */
    public int sizeBytes() {
        return source.length;
    }
}
