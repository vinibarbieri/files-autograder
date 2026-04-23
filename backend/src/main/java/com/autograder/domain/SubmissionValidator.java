package com.autograder.domain;

/**
 * First TDD target (see {@code docs/TDD_ROADMAP.md}, step 1).
 *
 * <p>Validates a {@link Submission} against the policies in
 * {@link SubmissionPolicy} before any I/O is done. Pure function — no
 * dependencies beyond the JDK, no collaborators to mock.
 *
 * <p><b>Intentionally unimplemented.</b> The red test in
 * {@code SubmissionValidatorTest} drives the first implementation. Do not
 * implement this class without first extending that test.
 */
public class SubmissionValidator {

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
        throw new UnsupportedOperationException(
                "SubmissionValidator.validate is the first TDD red target; "
                        + "implement it only when driven by a failing test.");
    }

    /**
     * @return the policy this validator enforces (exposed for tests/logs)
     */
    public SubmissionPolicy policy() {
        return policy;
    }
}
