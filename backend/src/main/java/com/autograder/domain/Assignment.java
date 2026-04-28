package com.autograder.domain;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Instructor-owned specification for one assignment.
 *
 * <p>Loaded by the {@code AssignmentRepository} port (cycle 5) and carried
 * through the evaluation pipeline. Immutable; all collection fields are defensively
 * copied on construction.
 *
 * @param id                     unique assignment identifier (matches {@code assignmentIdPattern} in {@link SubmissionPolicy})
 * @param compileFlags           extra flags passed to {@code gcc} (e.g. {@code "-Wall"}, {@code "-lm"})
 * @param timeout                hard wall-clock limit for the container execution
 * @param inputs                 stdin lines fed to the binary (one element per test run)
 * @param expectedStdout         expected standard output used by stdout-diffing strategies
 * @param expectedFiles          map of filename → expected content used by {@code DiffFileStrategy};
 *                               empty for assignments that use stdout comparison only
 * @param evaluationStrategyName logical name selecting the {@link EvaluationStrategy} to use
 */
public record Assignment(
        String id,
        List<String> compileFlags,
        Duration timeout,
        List<String> inputs,
        String expectedStdout,
        Map<String, String> expectedFiles,
        String evaluationStrategyName
) {

    public Assignment {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(compileFlags, "compileFlags");
        Objects.requireNonNull(timeout, "timeout");
        Objects.requireNonNull(inputs, "inputs");
        Objects.requireNonNull(expectedStdout, "expectedStdout");
        Objects.requireNonNull(expectedFiles, "expectedFiles");
        Objects.requireNonNull(evaluationStrategyName, "evaluationStrategyName");
        compileFlags = List.copyOf(compileFlags);
        inputs = List.copyOf(inputs);
        expectedFiles = Map.copyOf(expectedFiles);
    }
}
