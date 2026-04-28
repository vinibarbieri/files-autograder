package com.autograder.application;

import com.autograder.domain.Assignment;
import com.autograder.domain.ContainerRunner;
import com.autograder.domain.EvaluationResult;
import com.autograder.domain.EvaluationStrategy;
import com.autograder.domain.ExecutionResult;
import com.autograder.domain.Submission;

import java.util.Objects;

/**
 * Encapsulates a single grading request as an executable object.
 *
 * <p>Carries the {@link Submission} and {@link Assignment} needed for one
 * evaluation. Calling {@link #execute} drives the full pipeline:
 * container run → branch on outcome → delegate to the strategy.
 *
 * <p>Named design pattern: <b>Command</b> (ADR-0007). The command shape is
 * stable across Milestone 1 (immediate execution) and Milestone 2 (queued
 * execution) — only the executor changes, not this class.
 *
 * <p>Branching rules applied in {@link #execute}, in priority order:
 * <ol>
 *   <li>{@link ExecutionResult#timedOut()} → {@link com.autograder.domain.Verdict#TIMEOUT}
 *   <li>{@link ExecutionResult#exitCode()} ≠ 0 → {@link com.autograder.domain.Verdict#COMPILE_ERROR}
 *       with {@link ExecutionResult#stderr()} as the compile log
 *   <li>Otherwise → delegate to {@link EvaluationStrategy#evaluate}
 * </ol>
 */
public final class EvaluationCommand {

    private final Submission submission;
    private final Assignment assignment;

    /**
     * @param submission the student attempt (non-null)
     * @param assignment the instructor spec (non-null)
     */
    public EvaluationCommand(Submission submission, Assignment assignment) {
        this.submission = Objects.requireNonNull(submission, "submission");
        this.assignment = Objects.requireNonNull(assignment, "assignment");
    }

    /**
     * Runs the submission through the container and evaluates the result.
     *
     * @param runner   the container adapter to use for compilation and execution
     * @param strategy the evaluation strategy to apply on a clean exit
     * @return the domain verdict; never {@code null}
     */
    public EvaluationResult execute(ContainerRunner runner, EvaluationStrategy strategy) {
        Objects.requireNonNull(runner, "runner");
        Objects.requireNonNull(strategy, "strategy");

        ExecutionResult executionResult = runner.run(submission, assignment);

        if (executionResult.timedOut()) {
            return EvaluationResult.timeout();
        }

        if (executionResult.exitCode() != 0) {
            return EvaluationResult.compileError(executionResult.stderr());
        }

        return strategy.evaluate(executionResult, assignment);
    }

    /** @return the submission this command was created for */
    public Submission submission() {
        return submission;
    }

    /** @return the assignment this command was created for */
    public Assignment assignment() {
        return assignment;
    }
}
