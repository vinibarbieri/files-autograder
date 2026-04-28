package com.autograder.domain;

/**
 * Port for compiling and executing a student submission inside an isolated
 * container.
 *
 * <p>The single implementation for production use is
 * {@code DockerContainerRunner} (infrastructure, cycle 10). A scriptable
 * in-memory fake — {@code FakeContainerRunner} — lives in test sources
 * (cycle 7) and is used by application-layer tests.
 *
 * <p>Depends only on the JDK and other {@code domain} types (ADR-0008).
 * Named design pattern: the production implementation is a <b>Facade</b>
 * over {@code docker-java} (ADR-0007).
 *
 * @see ExecutionResult
 */
public interface ContainerRunner {

    /**
     * Compiles and runs the student's source code for the given assignment.
     *
     * <p>Returns a populated {@link ExecutionResult} regardless of outcome —
     * callers must inspect {@link ExecutionResult#timedOut()} and
     * {@link ExecutionResult#exitCode()} to distinguish timeout, compile
     * failure, and successful execution.
     *
     * @param submission the student attempt carrying the source bytes
     * @param assignment the instructor spec carrying compile flags and timeout
     * @return the raw execution result; never {@code null}
     */
    ExecutionResult run(Submission submission, Assignment assignment);
}
