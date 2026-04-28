package com.autograder.domain;

import java.util.regex.Pattern;

/**
 * First TDD target (see {@code docs/TDD_ROADMAP.md}, step 1).
 *
 * <p>Validates a {@link Submission} against the policies in
 * {@link SubmissionPolicy} before any I/O is done. Pure function — no
 * dependencies beyond the JDK, no collaborators to mock.
 *
 * <p>Roadmap cycle 1 — implemented green. Tests live in {@code SubmissionValidatorTest}.
 */
public class SubmissionValidator {

    private static final Pattern PROBLEM_FILE_NAME_PATTERN =
            Pattern.compile("^problem\\d+\\.c$");

    private final SubmissionPolicy policy;

    public SubmissionValidator(SubmissionPolicy policy) {
        this.policy = policy;
    }

    /**
     * Validates a submission. Returns silently on success; throws on failure.
     *
     * @throws InvalidSubmissionException when the submission violates policy
     */
    public void validate(Submission submission) {
        if (submission.source().length == 0) {
            throw new InvalidSubmissionException(
                    InvalidSubmissionException.Reason.EMPTY_SOURCE,
                    "Source file is empty.");
        }

        if (submission.sizeBytes() > policy.maxSourceSizeBytes()) {
            throw new InvalidSubmissionException(
                    InvalidSubmissionException.Reason.SOURCE_TOO_LARGE,
                    "Source size " + submission.sizeBytes()
                            + " bytes exceeds limit of " + policy.maxSourceSizeBytes() + " bytes.");
        }

        boolean extensionAllowed = policy.allowedExtensions().stream()
                .anyMatch(ext -> submission.fileName().endsWith(ext));
        boolean matchesProblemFileName = PROBLEM_FILE_NAME_PATTERN.matcher(submission.fileName()).matches();
        if (!extensionAllowed || !matchesProblemFileName) {
            throw new InvalidSubmissionException(
                    InvalidSubmissionException.Reason.DISALLOWED_EXTENSION,
                    "File name must match problemX.c where X is numeric: " + submission.fileName());
        }

        if (!submission.assignmentId().matches(policy.assignmentIdPattern())) {
            throw new InvalidSubmissionException(
                    InvalidSubmissionException.Reason.INVALID_ASSIGNMENT_ID,
                    "Assignment ID does not match required pattern: " + submission.assignmentId());
        }
    }

    /**
     * @return the policy this validator enforces (exposed for tests/logs)
     */
    public SubmissionPolicy policy() {
        return policy;
    }
}
