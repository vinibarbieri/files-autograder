package com.autograder.application;

import com.autograder.domain.Assignment;
import com.autograder.domain.AssignmentNotFoundException;
import com.autograder.domain.EvaluationResult;
import com.autograder.domain.EvaluationStrategy;
import com.autograder.domain.ExecutionResult;
import com.autograder.domain.InvalidSubmissionException;
import com.autograder.domain.Submission;
import com.autograder.domain.SubmissionPolicy;
import com.autograder.domain.SubmissionValidator;
import com.autograder.domain.Verdict;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RED tests for roadmap cycle 8: {@link EvaluateSubmissionService}.
 *
 * <p>Plain JUnit 5 — no Spring context. All dependencies are hand-wired
 * with the fakes from cycles 5 and 7. Deviation from the roadmap's
 * {@code @SpringBootTest} suggestion: the Spring context cannot boot until
 * infrastructure adapters exist (cycles 10–11); plain tests give equivalent
 * coverage without the overhead. Context wiring is verified by the existing
 * {@code AutograderApplicationContextTest} smoke test once adapters land.
 */
class EvaluateSubmissionServiceTest {

    private InMemoryAssignmentRepository repository;
    private FakeContainerRunner runner;
    private EvaluateSubmissionService service;

    /** Strategy that always passes — used as the default for happy-path tests. */
    private static final EvaluationStrategy PASS_STRATEGY =
            (exec, asgn) -> EvaluationResult.pass();

    /** Strategy that always fails with a fixed diff. */
    private static final EvaluationStrategy FAIL_STRATEGY =
            (exec, asgn) -> EvaluationResult.fail("--- expected\n+++ actual\n-expected\n+actual\n");

    @BeforeEach
    void setUp() {
        repository = new InMemoryAssignmentRepository();
        runner     = new FakeContainerRunner(cleanResult(""));
        service    = new EvaluateSubmissionService(
                new SubmissionValidator(SubmissionPolicy.defaults()),
                repository,
                runner,
                Map.of("diff-stdout", PASS_STRATEGY));
    }

    // ---------- happy paths ----------

    @Test
    @DisplayName("valid submission, known assignment, clean run → PASS")
    void happy_path_returns_pass() {
        repository.store(assignment("hw01"));
        runner.willReturn(cleanResult("expected\n"));

        EvaluationResult result = service.evaluate(submission("hw01"));

        assertThat(result.verdict()).isEqualTo(Verdict.PASS);
    }

    @Test
    @DisplayName("valid submission, clean run, failing strategy → FAIL with diff")
    void happy_path_failing_strategy_returns_fail() {
        repository.store(assignment("hw01"));
        runner.willReturn(cleanResult("wrong\n"));
        EvaluateSubmissionService svc = serviceWith(Map.of("diff-stdout", FAIL_STRATEGY));

        EvaluationResult result = svc.evaluate(submission("hw01"));

        assertThat(result.verdict()).isEqualTo(Verdict.FAIL);
        assertThat(result.diff()).isPresent();
    }

    // ---------- error branches ----------

    @Test
    @DisplayName("unknown assignment id → AssignmentNotFoundException")
    void unknown_assignment_throws_not_found() {
        // repository is empty — no assignment stored

        assertThatThrownBy(() -> service.evaluate(submission("hw99")))
                .isInstanceOf(AssignmentNotFoundException.class)
                .hasMessageContaining("hw99");
    }

    @Test
    @DisplayName("AssignmentNotFoundException carries the id that was looked up")
    void not_found_exception_carries_id() {
        assertThatThrownBy(() -> service.evaluate(submission("hw99")))
                .isInstanceOf(AssignmentNotFoundException.class)
                .satisfies(ex -> assertThat(((AssignmentNotFoundException) ex).assignmentId())
                        .isEqualTo("hw99"));
    }

    @Test
    @DisplayName("invalid submission → InvalidSubmissionException before any I/O")
    void invalid_submission_throws_before_io() {
        // empty source triggers validation failure
        Submission bad = new Submission("hw01", "problem1.c", new byte[0], "127.0.0.1", Instant.now());

        assertThatThrownBy(() -> service.evaluate(bad))
                .isInstanceOf(InvalidSubmissionException.class);
    }

    @Test
    @DisplayName("invalid submission: runner is never called")
    void invalid_submission_does_not_call_runner() {
        Submission bad = new Submission("hw01", "problem1.c", new byte[0], "127.0.0.1", Instant.now());

        try { service.evaluate(bad); } catch (InvalidSubmissionException ignored) {}

        assertThat(runner.lastSubmission()).isNull();
    }

    // ---------- verdict propagation ----------

    @Test
    @DisplayName("timed-out container run → TIMEOUT verdict")
    void timeout_surfaces_as_timeout_verdict() {
        repository.store(assignment("hw01"));
        runner.willReturn(new ExecutionResult(124, "", "", Map.of(), Duration.ofSeconds(10), true));

        EvaluationResult result = service.evaluate(submission("hw01"));

        assertThat(result.verdict()).isEqualTo(Verdict.TIMEOUT);
    }

    @Test
    @DisplayName("non-zero exit code → COMPILE_ERROR verdict")
    void compile_error_surfaces_as_compile_error_verdict() {
        repository.store(assignment("hw01"));
        runner.willReturn(new ExecutionResult(1, "", "error: missing semicolon", Map.of(), Duration.ofMillis(200), false));

        EvaluationResult result = service.evaluate(submission("hw01"));

        assertThat(result.verdict()).isEqualTo(Verdict.COMPILE_ERROR);
        assertThat(result.compileLog()).hasValueSatisfying(log ->
                assertThat(log).contains("missing semicolon"));
    }

    @Test
    @DisplayName("unknown strategy name → INTERNAL_ERROR verdict")
    void unknown_strategy_name_returns_internal_error() {
        // assignment references a strategy not in the service's map
        repository.store(new Assignment("hw01", List.of(), Duration.ofSeconds(10),
                List.of(), "", Map.of(), "nonexistent-strategy"));
        runner.willReturn(cleanResult(""));

        EvaluationResult result = service.evaluate(submission("hw01"));

        assertThat(result.verdict()).isEqualTo(Verdict.INTERNAL_ERROR);
        assertThat(result.compileLog()).hasValueSatisfying(msg ->
                assertThat(msg).contains("nonexistent-strategy"));
    }

    // ---------- helpers ----------

    private EvaluateSubmissionService serviceWith(Map<String, EvaluationStrategy> strategies) {
        return new EvaluateSubmissionService(
                new SubmissionValidator(SubmissionPolicy.defaults()),
                repository,
                runner,
                strategies);
    }

    private static ExecutionResult cleanResult(String stdout) {
        return new ExecutionResult(0, stdout, "", Map.of(), Duration.ofMillis(100), false);
    }

    private static Submission submission(String assignmentId) {
        return new Submission(
                assignmentId,
                "problem1.c",
                "int main(){return 0;}".getBytes(),
                "127.0.0.1",
                Instant.now());
    }

    private static Assignment assignment(String id) {
        return new Assignment(id, List.of(), Duration.ofSeconds(10),
                List.of(), "", Map.of(), "diff-stdout");
    }
}
