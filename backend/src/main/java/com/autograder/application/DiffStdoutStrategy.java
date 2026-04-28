package com.autograder.application;

import com.autograder.domain.Assignment;
import com.autograder.domain.EvaluationResult;
import com.autograder.domain.EvaluationStrategy;
import com.autograder.domain.ExecutionResult;

/**
 * {@link EvaluationStrategy} that compares the container's stdout against
 * the assignment's expected stdout after normalising line endings and
 * stripping trailing whitespace.
 *
 * <p>Normalisation rules applied to both sides before comparison:
 * <ol>
 *   <li>CRLF ({@code \r\n}) → LF ({@code \n})
 *   <li>Trailing whitespace stripped from the end of the entire string
 * </ol>
 *
 * <p>If the execution timed out the result is {@link com.autograder.domain.Verdict#TIMEOUT}
 * regardless of any partial stdout captured.
 */
public class DiffStdoutStrategy implements EvaluationStrategy {

    @Override
    public EvaluationResult evaluate(ExecutionResult executionResult, Assignment assignment) {
        if (executionResult.timedOut()) {
            return EvaluationResult.timeout();
        }

        String actual   = normalize(executionResult.stdout());
        String expected = normalize(assignment.expectedStdout());

        if (actual.equals(expected)) {
            return EvaluationResult.pass();
        }

        return EvaluationResult.fail(buildDiff(expected, actual));
    }

    // ---------- private helpers ----------

    private static String normalize(String s) {
        String normalized = s.replace("\r\n", "\n");
        int end = normalized.length();
        while (end > 0 && (normalized.charAt(end - 1) == '\n' || normalized.charAt(end - 1) == '\r')) {
            end--;
        }
        return normalized.substring(0, end);
    }

    /**
     * Produces a minimal human-readable diff header followed by line-level
     * {@code -}/{@code +} markers. Not a full unified diff; sufficient for
     * Milestone 1 diagnostics.
     */
    private static String buildDiff(String expected, String actual) {
        StringBuilder sb = new StringBuilder("--- expected\n+++ actual\n");

        String[] expLines = splitLines(expected);
        String[] actLines = splitLines(actual);
        int max = Math.max(expLines.length, actLines.length);

        for (int i = 0; i < max; i++) {
            if (i < expLines.length) sb.append('-').append(expLines[i]).append('\n');
            if (i < actLines.length) sb.append('+').append(actLines[i]).append('\n');
        }

        return sb.toString();
    }

    private static String[] splitLines(String s) {
        return s.isEmpty() ? new String[0] : s.split("\n", -1);
    }
}
