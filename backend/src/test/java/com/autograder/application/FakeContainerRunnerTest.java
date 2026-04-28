package com.autograder.application;

import com.autograder.domain.Assignment;
import com.autograder.domain.ContainerRunner;
import com.autograder.domain.ExecutionResult;
import com.autograder.domain.Submission;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * RED tests for roadmap cycle 7: {@link FakeContainerRunner} test double.
 *
 * <p>All assertions are made through the {@link ContainerRunner} port so that
 * the interface contract is what is verified.
 */
class FakeContainerRunnerTest {

    @Test
    @DisplayName("run() returns the configured result")
    void run_returns_configured_result() {
        ExecutionResult configured = cleanResult("hello\n");
        ContainerRunner runner = new FakeContainerRunner(configured);

        ExecutionResult actual = runner.run(submission(), assignment());

        assertThat(actual).isSameAs(configured);
    }

    @Test
    @DisplayName("willReturn() swaps the canned result for subsequent calls")
    void willReturn_changes_canned_result() {
        FakeContainerRunner runner = new FakeContainerRunner(cleanResult("first\n"));
        ExecutionResult second = cleanResult("second\n");

        runner.willReturn(second);

        assertThat(runner.run(submission(), assignment())).isSameAs(second);
    }

    @Test
    @DisplayName("run() records the submission it was called with")
    void run_records_last_submission() {
        FakeContainerRunner runner = new FakeContainerRunner(cleanResult(""));
        Submission sub = submission();

        runner.run(sub, assignment());

        assertThat(runner.lastSubmission()).isSameAs(sub);
    }

    @Test
    @DisplayName("run() records the assignment it was called with")
    void run_records_last_assignment() {
        FakeContainerRunner runner = new FakeContainerRunner(cleanResult(""));
        Assignment asgn = assignment();

        runner.run(submission(), asgn);

        assertThat(runner.lastAssignment()).isSameAs(asgn);
    }

    @Test
    @DisplayName("lastSubmission() is null before any call")
    void last_submission_null_before_any_call() {
        FakeContainerRunner runner = new FakeContainerRunner(cleanResult(""));

        assertThat(runner.lastSubmission()).isNull();
    }

    @Test
    @DisplayName("lastAssignment() is null before any call")
    void last_assignment_null_before_any_call() {
        FakeContainerRunner runner = new FakeContainerRunner(cleanResult(""));

        assertThat(runner.lastAssignment()).isNull();
    }

    @Test
    @DisplayName("constructor rejects null result")
    void constructor_rejects_null_result() {
        assertThatNullPointerException()
                .isThrownBy(() -> new FakeContainerRunner(null));
    }

    @Test
    @DisplayName("willReturn() rejects null result")
    void willReturn_rejects_null_result() {
        FakeContainerRunner runner = new FakeContainerRunner(cleanResult(""));

        assertThatNullPointerException()
                .isThrownBy(() -> runner.willReturn(null));
    }

    // ---------- helpers ----------

    private static ExecutionResult cleanResult(String stdout) {
        return new ExecutionResult(0, stdout, "", Map.of(), Duration.ofMillis(50), false);
    }

    private static Submission submission() {
        return new Submission(
                "hw01",
                "main.c",
                "int main(){return 0;}".getBytes(),
                "127.0.0.1",
                Instant.now());
    }

    private static Assignment assignment() {
        return new Assignment(
                "hw01",
                List.of(),
                Duration.ofSeconds(10),
                List.of(),
                "",
                Map.of(),
                "diff-stdout");
    }
}
