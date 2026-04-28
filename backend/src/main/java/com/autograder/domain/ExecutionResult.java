package com.autograder.domain;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * Raw output captured from a single container execution.
 *
 * <p>Produced by the {@code ContainerRunner} port and consumed by
 * {@link EvaluationStrategy} implementations. Carries no domain judgement —
 * it is a plain data transfer object between the infrastructure adapter and
 * the application layer.
 *
 * @param exitCode      process exit code (0 = clean exit)
 * @param stdout        complete stdout captured from the container
 * @param stderr        complete stderr captured from the container
 * @param producedFiles map of filename → content for file-based strategies (see {@code DiffFileStrategy})
 * @param duration      wall-clock time the container ran
 * @param timedOut      {@code true} when the hard timeout was reached before the process exited
 */
public record ExecutionResult(
        int exitCode,
        String stdout,
        String stderr,
        Map<String, String> producedFiles,
        Duration duration,
        boolean timedOut
) {

    public ExecutionResult {
        Objects.requireNonNull(stdout, "stdout");
        Objects.requireNonNull(stderr, "stderr");
        Objects.requireNonNull(producedFiles, "producedFiles");
        Objects.requireNonNull(duration, "duration");
        producedFiles = Map.copyOf(producedFiles);
    }
}
