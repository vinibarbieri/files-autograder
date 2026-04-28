package com.autograder.application;

import com.autograder.domain.Assignment;
import com.autograder.domain.ContainerRunner;
import com.autograder.domain.ExecutionResult;
import com.autograder.domain.Submission;

import java.util.Objects;

/**
 * Scriptable {@link ContainerRunner} test double for use in
 * application-layer unit tests.
 *
 * <p>Pre-programmed with a canned {@link ExecutionResult} at construction;
 * the result can be swapped mid-test via {@link #willReturn}. Records the
 * arguments of the most recent {@link #run} call for post-act assertions.
 *
 * <p>Not thread-safe — test use only.
 */
public class FakeContainerRunner implements ContainerRunner {

    private ExecutionResult cannedResult;
    private Submission lastSubmission;
    private Assignment lastAssignment;

    /**
     * @param result the {@link ExecutionResult} to return for every {@link #run} call
     */
    public FakeContainerRunner(ExecutionResult result) {
        this.cannedResult = Objects.requireNonNull(result, "result");
    }

    /**
     * Replaces the canned result returned by subsequent {@link #run} calls.
     *
     * @param result the new result to return (non-null)
     */
    public void willReturn(ExecutionResult result) {
        this.cannedResult = Objects.requireNonNull(result, "result");
    }

    @Override
    public ExecutionResult run(Submission submission, Assignment assignment) {
        this.lastSubmission = submission;
        this.lastAssignment = assignment;
        return cannedResult;
    }

    /**
     * @return the {@link Submission} passed to the most recent {@link #run} call,
     *         or {@code null} if {@link #run} has not been called yet
     */
    public Submission lastSubmission() {
        return lastSubmission;
    }

    /**
     * @return the {@link Assignment} passed to the most recent {@link #run} call,
     *         or {@code null} if {@link #run} has not been called yet
     */
    public Assignment lastAssignment() {
        return lastAssignment;
    }
}
