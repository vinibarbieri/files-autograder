package com.autograder.domain;

/**
 * Strategy port for turning a raw {@link ExecutionResult} into a domain verdict.
 *
 * <p>Implementations live in the {@code application} layer (ADR-0008). The
 * interface lives in {@code domain} so that the {@code EvaluationCommand} (also
 * in {@code application}) can depend on it without creating a circular dependency.
 *
 * <p>Named design pattern: <b>Strategy</b> (ADR-0007).
 */
public interface EvaluationStrategy {

    /**
     * Evaluates the execution result against the assignment specification.
     *
     * @param executionResult raw output from the container run
     * @param assignment      the instructor-owned spec containing expected outputs
     * @return the domain verdict; never {@code null}
     */
    EvaluationResult evaluate(ExecutionResult executionResult, Assignment assignment);
}
