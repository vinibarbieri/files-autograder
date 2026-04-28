package com.autograder.application;

import com.autograder.domain.Assignment;
import com.autograder.domain.EvaluationResult;
import com.autograder.domain.ExecutionResult;
import com.autograder.domain.Verdict;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RED tests for roadmap cycle 3: {@link DiffStdoutStrategy}.
 *
 * <p>No mocks, no Spring, no I/O. Domain types are constructed directly.
 */
class DiffStdoutStrategyTest {

    private final DiffStdoutStrategy strategy = new DiffStdoutStrategy();

    @Test
    @DisplayName("identical stdout → PASS")
    void identical_stdout_passes() {
        EvaluationResult result = strategy.evaluate(
                executionResult("hello\n", false),
                assignment("hello\n"));

        assertThat(result.verdict()).isEqualTo(Verdict.PASS);
    }

    @Test
    @DisplayName("different stdout → FAIL with diff present")
    void different_stdout_fails_with_diff() {
        EvaluationResult result = strategy.evaluate(
                executionResult("actual\n", false),
                assignment("expected\n"));

        assertThat(result.verdict()).isEqualTo(Verdict.FAIL);
        assertThat(result.diff()).isPresent();
    }

    @Test
    @DisplayName("trailing newline on actual is normalised → PASS")
    void trailing_newline_difference_is_normalised() {
        EvaluationResult result = strategy.evaluate(
                executionResult("hello\n", false),
                assignment("hello"));

        assertThat(result.verdict()).isEqualTo(Verdict.PASS);
    }

    @Test
    @DisplayName("CRLF in actual is normalised to LF → PASS")
    void crlf_in_actual_normalised_to_lf() {
        EvaluationResult result = strategy.evaluate(
                executionResult("hello\r\nworld\r\n", false),
                assignment("hello\nworld"));

        assertThat(result.verdict()).isEqualTo(Verdict.PASS);
    }

    @Test
    @DisplayName("CRLF in expected is normalised to LF → PASS")
    void crlf_in_expected_normalised_to_lf() {
        EvaluationResult result = strategy.evaluate(
                executionResult("hello\nworld\n", false),
                assignment("hello\r\nworld\r\n"));

        assertThat(result.verdict()).isEqualTo(Verdict.PASS);
    }

    @Test
    @DisplayName("empty expected and empty actual → PASS")
    void empty_expected_and_empty_actual_passes() {
        EvaluationResult result = strategy.evaluate(
                executionResult("", false),
                assignment(""));

        assertThat(result.verdict()).isEqualTo(Verdict.PASS);
    }

    @Test
    @DisplayName("empty expected, non-empty actual → FAIL with diff present")
    void empty_expected_nonempty_actual_fails() {
        EvaluationResult result = strategy.evaluate(
                executionResult("something\n", false),
                assignment(""));

        assertThat(result.verdict()).isEqualTo(Verdict.FAIL);
        assertThat(result.diff()).isPresent();
    }

    @Test
    @DisplayName("timed-out execution → TIMEOUT regardless of stdout")
    void timedout_execution_returns_timeout() {
        EvaluationResult result = strategy.evaluate(
                executionResult("partial output", true),
                assignment("expected"));

        assertThat(result.verdict()).isEqualTo(Verdict.TIMEOUT);
    }

    @Test
    @DisplayName("diff content contains both expected and actual lines")
    void diff_content_contains_expected_and_actual() {
        EvaluationResult result = strategy.evaluate(
                executionResult("actual line\n", false),
                assignment("expected line\n"));

        assertThat(result.diff()).hasValueSatisfying(diff -> {
            assertThat(diff).contains("expected line");
            assertThat(diff).contains("actual line");
        });
    }

    @Test
    @DisplayName("trailing space is significant and must fail")
    void trailing_space_difference_fails() {
        EvaluationResult result = strategy.evaluate(
                executionResult("hello \n", false),
                assignment("hello\n"));

        assertThat(result.verdict()).isEqualTo(Verdict.FAIL);
        assertThat(result.diff()).isPresent();
    }

    // ---------- helpers ----------

    private static ExecutionResult executionResult(String stdout, boolean timedOut) {
        return new ExecutionResult(
                timedOut ? 124 : 0,
                stdout,
                "",
                Map.of(),
                Duration.ofMillis(50),
                timedOut);
    }

    private static Assignment assignment(String expectedStdout) {
        return new Assignment(
                "hw01",
                List.of(),
                Duration.ofSeconds(10),
                List.of(),
                expectedStdout,
                "diff-stdout");
    }
}
