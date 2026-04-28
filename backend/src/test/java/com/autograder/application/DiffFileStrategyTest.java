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
 * RED tests for roadmap cycle 4: {@link DiffFileStrategy}.
 *
 * <p>No mocks, no Spring, no filesystem access. The strategy reads only from
 * the in-memory {@code ExecutionResult.producedFiles} map.
 */
class DiffFileStrategyTest {

    private final DiffFileStrategy strategy = new DiffFileStrategy();

    @Test
    @DisplayName("produced file matches expected → PASS")
    void matching_file_passes() {
        EvaluationResult result = strategy.evaluate(
                executionResult(Map.of("output.txt", "hello\n")),
                assignment(Map.of("output.txt", "hello\n")));

        assertThat(result.verdict()).isEqualTo(Verdict.PASS);
    }

    @Test
    @DisplayName("produced file content differs → FAIL with diff")
    void different_file_content_fails_with_diff() {
        EvaluationResult result = strategy.evaluate(
                executionResult(Map.of("output.txt", "actual\n")),
                assignment(Map.of("output.txt", "expected\n")));

        assertThat(result.verdict()).isEqualTo(Verdict.FAIL);
        assertThat(result.diff()).isPresent();
    }

    @Test
    @DisplayName("expected file absent from producedFiles → FAIL with diff")
    void missing_produced_file_fails() {
        EvaluationResult result = strategy.evaluate(
                executionResult(Map.of()),
                assignment(Map.of("output.txt", "expected\n")));

        assertThat(result.verdict()).isEqualTo(Verdict.FAIL);
        assertThat(result.diff()).isPresent();
    }

    @Test
    @DisplayName("timed-out execution → TIMEOUT regardless of produced files")
    void timedout_returns_timeout() {
        EvaluationResult result = strategy.evaluate(
                executionResultTimedOut(Map.of("output.txt", "hello\n")),
                assignment(Map.of("output.txt", "hello\n")));

        assertThat(result.verdict()).isEqualTo(Verdict.TIMEOUT);
    }

    @Test
    @DisplayName("diff contains both expected and actual content")
    void diff_contains_expected_and_actual_content() {
        EvaluationResult result = strategy.evaluate(
                executionResult(Map.of("out.txt", "actual line\n")),
                assignment(Map.of("out.txt", "expected line\n")));

        assertThat(result.diff()).hasValueSatisfying(diff -> {
            assertThat(diff).contains("expected line");
            assertThat(diff).contains("actual line");
        });
    }

    @Test
    @DisplayName("diff mentions the filename")
    void diff_mentions_filename() {
        EvaluationResult result = strategy.evaluate(
                executionResult(Map.of("result.txt", "wrong\n")),
                assignment(Map.of("result.txt", "correct\n")));

        assertThat(result.diff()).hasValueSatisfying(diff ->
                assertThat(diff).contains("result.txt"));
    }

    @Test
    @DisplayName("all expected files match → PASS")
    void all_expected_files_match_passes() {
        EvaluationResult result = strategy.evaluate(
                executionResult(Map.of("a.txt", "foo\n", "b.txt", "bar\n")),
                assignment(Map.of("a.txt", "foo\n", "b.txt", "bar\n")));

        assertThat(result.verdict()).isEqualTo(Verdict.PASS);
    }

    @Test
    @DisplayName("one file differs among many → FAIL")
    void one_file_differs_among_many_fails() {
        EvaluationResult result = strategy.evaluate(
                executionResult(Map.of("a.txt", "foo\n", "b.txt", "WRONG\n")),
                assignment(Map.of("a.txt", "foo\n", "b.txt", "bar\n")));

        assertThat(result.verdict()).isEqualTo(Verdict.FAIL);
        assertThat(result.diff()).isPresent();
    }

    @Test
    @DisplayName("CRLF in produced file normalised to LF → PASS")
    void crlf_in_produced_file_normalised() {
        EvaluationResult result = strategy.evaluate(
                executionResult(Map.of("output.txt", "hello\r\nworld\r\n")),
                assignment(Map.of("output.txt", "hello\nworld")));

        assertThat(result.verdict()).isEqualTo(Verdict.PASS);
    }

    @Test
    @DisplayName("trailing newline difference in file is normalised → PASS")
    void trailing_newline_in_file_normalised() {
        EvaluationResult result = strategy.evaluate(
                executionResult(Map.of("output.txt", "hello\n")),
                assignment(Map.of("output.txt", "hello")));

        assertThat(result.verdict()).isEqualTo(Verdict.PASS);
    }

    @Test
    @DisplayName("producedFiles contains undeclared file → FAIL with diff")
    void undeclared_produced_file_fails() {
        EvaluationResult result = strategy.evaluate(
                executionResult(Map.of("output.txt", "hello\n", "extra.txt", "surprise\n")),
                assignment(Map.of("output.txt", "hello\n")));

        assertThat(result.verdict()).isEqualTo(Verdict.FAIL);
        assertThat(result.diff()).hasValueSatisfying(diff ->
                assertThat(diff).contains("extra.txt"));
    }

    @Test
    @DisplayName("undeclared file diff marks expected side as absent")
    void undeclared_produced_file_diff_shows_unexpected_content() {
        EvaluationResult result = strategy.evaluate(
                executionResult(Map.of("output.txt", "hello\n", "rogue.txt", "bad\n")),
                assignment(Map.of("output.txt", "hello\n")));

        assertThat(result.diff()).hasValueSatisfying(diff -> {
            assertThat(diff).contains("rogue.txt");
            assertThat(diff).contains("bad");
        });
    }

    // ---------- helpers ----------

    private static ExecutionResult executionResult(Map<String, String> producedFiles) {
        return new ExecutionResult(0, "", "", producedFiles, Duration.ofMillis(50), false);
    }

    private static ExecutionResult executionResultTimedOut(Map<String, String> producedFiles) {
        return new ExecutionResult(124, "", "", producedFiles, Duration.ofMillis(5000), true);
    }

    private static Assignment assignment(Map<String, String> expectedFiles) {
        return new Assignment(
                "hw02",
                List.of(),
                Duration.ofSeconds(10),
                List.of(),
                "",
                expectedFiles,
                "diff-file");
    }
}
