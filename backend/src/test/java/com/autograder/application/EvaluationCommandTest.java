package com.autograder.application;

import com.autograder.domain.Assignment;
import com.autograder.domain.ContainerRunner;
import com.autograder.domain.EvaluationResult;
import com.autograder.domain.EvaluationStrategy;
import com.autograder.domain.ExecutionResult;
import com.autograder.domain.Submission;
import com.autograder.domain.Verdict;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RED tests for roadmap cycle 6: {@link EvaluationCommand}.
 *
 * <p>No mocks, no Spring, no I/O. {@link ContainerRunner} and
 * {@link EvaluationStrategy} are provided as lambdas returning canned values.
 */
class EvaluationCommandTest {

    // ---------- compile error ----------

    @Test
    @DisplayName("non-zero exit code → COMPILE_ERROR")
    void nonzero_exit_code_returns_compile_error() {
        EvaluationCommand cmd = command();
        ContainerRunner runner = (sub, asgn) -> executionResult(1, "error: undeclared identifier", false);

        EvaluationResult result = cmd.execute(runner, passingStrategy());

        assertThat(result.verdict()).isEqualTo(Verdict.COMPILE_ERROR);
    }

    @Test
    @DisplayName("compile error result carries stderr as compileLog")
    void compile_error_carries_stderr() {
        EvaluationCommand cmd = command();
        ContainerRunner runner = (sub, asgn) -> executionResult(1, "main.c:3: error", false);

        EvaluationResult result = cmd.execute(runner, passingStrategy());

        assertThat(result.compileLog()).hasValueSatisfying(log ->
                assertThat(log).contains("main.c:3: error"));
    }

    // ---------- timeout ----------

    @Test
    @DisplayName("timedOut flag → TIMEOUT")
    void timedout_execution_returns_timeout() {
        EvaluationCommand cmd = command();
        ContainerRunner runner = (sub, asgn) -> executionResult(124, "", true);

        EvaluationResult result = cmd.execute(runner, passingStrategy());

        assertThat(result.verdict()).isEqualTo(Verdict.TIMEOUT);
    }

    @Test
    @DisplayName("timeout takes priority over non-zero exit code")
    void timeout_takes_priority_over_nonzero_exit_code() {
        EvaluationCommand cmd = command();
        ContainerRunner runner = (sub, asgn) -> executionResult(1, "some error", true);

        EvaluationResult result = cmd.execute(runner, passingStrategy());

        assertThat(result.verdict()).isEqualTo(Verdict.TIMEOUT);
    }

    // ---------- strategy delegation ----------

    @Test
    @DisplayName("clean execution + passing strategy → PASS")
    void clean_execution_with_passing_strategy_returns_pass() {
        EvaluationCommand cmd = command();
        ContainerRunner runner = (sub, asgn) -> executionResult(0, "", false);

        EvaluationResult result = cmd.execute(runner, passingStrategy());

        assertThat(result.verdict()).isEqualTo(Verdict.PASS);
    }

    @Test
    @DisplayName("clean execution + failing strategy → FAIL with diff")
    void clean_execution_with_failing_strategy_returns_fail() {
        EvaluationCommand cmd = command();
        ContainerRunner runner  = (sub, asgn) -> executionResult(0, "wrong\n", false);
        EvaluationStrategy fail = (exec, asgn) -> EvaluationResult.fail("--- expected\n+++ actual\n");

        EvaluationResult result = cmd.execute(runner, fail);

        assertThat(result.verdict()).isEqualTo(Verdict.FAIL);
        assertThat(result.diff()).isPresent();
    }

    @Test
    @DisplayName("runner is called with the command's submission and assignment")
    void runner_receives_commands_submission_and_assignment() {
        Submission sub  = submission();
        Assignment asgn = assignment();
        EvaluationCommand cmd = new EvaluationCommand(sub, asgn);

        Submission[] capturedSub  = new Submission[1];
        Assignment[] capturedAsgn = new Assignment[1];
        ContainerRunner runner = (s, a) -> {
            capturedSub[0]  = s;
            capturedAsgn[0] = a;
            return executionResult(0, "", false);
        };

        cmd.execute(runner, passingStrategy());

        assertThat(capturedSub[0]).isSameAs(sub);
        assertThat(capturedAsgn[0]).isSameAs(asgn);
    }

    // ---------- helpers ----------

    private static EvaluationCommand command() {
        return new EvaluationCommand(submission(), assignment());
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
                List.of("-Wall"),
                Duration.ofSeconds(10),
                List.of(),
                "expected output\n",
                Map.of(),
                "diff-stdout");
    }

    private static ExecutionResult executionResult(int exitCode, String stderr, boolean timedOut) {
        return new ExecutionResult(exitCode, "", stderr, Map.of(), Duration.ofMillis(100), timedOut);
    }

    private static EvaluationStrategy passingStrategy() {
        return (exec, asgn) -> EvaluationResult.pass();
    }
}
