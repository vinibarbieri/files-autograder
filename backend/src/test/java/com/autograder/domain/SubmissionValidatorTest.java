package com.autograder.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * FIRST RED TEST for the TDD roadmap (see docs/TDD_ROADMAP.md, step 1).
 *
 * <p>This suite is intentionally failing right now: {@link SubmissionValidator}
 * only throws {@code UnsupportedOperationException}. Implementation proceeds by
 * making these tests pass one by one — start with
 * {@code accepts_wellFormedSubmission}, let the others stay red, then light
 * them up one failure class at a time. That ordering is the exercise.
 */
class SubmissionValidatorTest {

    private final SubmissionValidator validator =
            new SubmissionValidator(SubmissionPolicy.defaults());

    @Test
    @DisplayName("accepts a well-formed submission")
    void accepts_wellFormedSubmission() {
        Submission submission = submission("hw03", "problem3.c", "int main(){return 0;}");

        assertThatCode(() -> validator.validate(submission))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("rejects empty source")
    void rejects_emptySource() {
        Submission submission = submission("hw03", "main.c", "");

        assertThatThrownBy(() -> validator.validate(submission))
                .isInstanceOf(InvalidSubmissionException.class)
                .extracting(e -> ((InvalidSubmissionException) e).reason())
                .isEqualTo(InvalidSubmissionException.Reason.EMPTY_SOURCE);
    }

    @Test
    @DisplayName("rejects source larger than the policy cap")
    void rejects_oversizedSource() {
        byte[] tooBig = new byte[SubmissionPolicy.defaults().maxSourceSizeBytes() + 1];
        Submission submission = new Submission(
                "hw03", "main.c", tooBig, "127.0.0.1", Instant.now());

        assertThatThrownBy(() -> validator.validate(submission))
                .isInstanceOf(InvalidSubmissionException.class)
                .extracting(e -> ((InvalidSubmissionException) e).reason())
                .isEqualTo(InvalidSubmissionException.Reason.SOURCE_TOO_LARGE);
    }

    @Test
    @DisplayName("rejects filename that does not match problemX.c")
    void rejects_invalidFileNamePattern() {
        Submission submission = submission("hw03", "main.c", "int main(){return 0;}");

        assertThatThrownBy(() -> validator.validate(submission))
                .isInstanceOf(InvalidSubmissionException.class)
                .extracting(e -> ((InvalidSubmissionException) e).reason())
                .isEqualTo(InvalidSubmissionException.Reason.DISALLOWED_EXTENSION);
    }

    @Test
    @DisplayName("rejects non-numeric problem suffix")
    void rejects_nonNumericProblemSuffix() {
        Submission submission = submission("hw03", "problemX.c", "int main(){return 0;}");

        assertThatThrownBy(() -> validator.validate(submission))
                .isInstanceOf(InvalidSubmissionException.class)
                .extracting(e -> ((InvalidSubmissionException) e).reason())
                .isEqualTo(InvalidSubmissionException.Reason.DISALLOWED_EXTENSION);
    }

    @Test
    @DisplayName("rejects assignment id that does not match the policy pattern")
    void rejects_invalidAssignmentId() {
        Submission submission = submission("hw 03!", "problem3.c", "int main(){return 0;}");

        assertThatThrownBy(() -> validator.validate(submission))
                .isInstanceOf(InvalidSubmissionException.class)
                .extracting(e -> ((InvalidSubmissionException) e).reason())
                .isEqualTo(InvalidSubmissionException.Reason.INVALID_ASSIGNMENT_ID);
    }

    @Test
    @DisplayName("policy is exposed for diagnostics")
    void exposes_policy() {
        assertThat(validator.policy()).isEqualTo(SubmissionPolicy.defaults());
    }

    // ---------- helpers ----------

    private static Submission submission(String assignmentId, String fileName, String body) {
        return new Submission(
                assignmentId,
                fileName,
                body.getBytes(StandardCharsets.UTF_8),
                "127.0.0.1",
                Instant.now()
        );
    }
}
