package com.autograder.application;

import com.autograder.domain.Assignment;
import com.autograder.domain.EvaluationResult;
import com.autograder.domain.EvaluationStrategy;
import com.autograder.domain.ExecutionResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

/**
 * {@link EvaluationStrategy} that compares files produced by the container
 * against the expected files declared in the assignment spec.
 *
 * <p>Comparison is driven entirely by the in-memory
 * {@link ExecutionResult#producedFiles()} map — no filesystem access is
 * performed. Normalisation rules (CRLF→LF, trailing newlines stripped) are
 * applied to both sides before comparison, matching the behaviour of
 * {@link DiffStdoutStrategy}.
 *
 * <p>If the execution timed out the result is
 * {@link com.autograder.domain.Verdict#TIMEOUT} regardless of any files
 * captured. If any expected file is absent, its content differs, or the
 * container produced files not declared in the assignment spec, the result
 * is {@link com.autograder.domain.Verdict#FAIL} with a diff that names the
 * file(s) involved.
 */
@Component("diff-file")
public class DiffFileStrategy implements EvaluationStrategy {

    @Override
    public EvaluationResult evaluate(ExecutionResult executionResult, Assignment assignment) {
        if (executionResult.timedOut()) {
            return EvaluationResult.timeout();
        }

        Map<String, String> expectedFiles = assignment.expectedFiles();
        Map<String, String> producedFiles = executionResult.producedFiles();

        // Iterate in deterministic order so diffs are stable across runs.
        StringBuilder combinedDiff = new StringBuilder();

        // Check expected files: missing or content mismatch.
        for (String filename : new TreeMap<>(expectedFiles).keySet()) {
            String expected = normalize(expectedFiles.get(filename));
            String actual   = producedFiles.containsKey(filename)
                    ? normalize(producedFiles.get(filename))
                    : "";

            if (!actual.equals(expected)) {
                combinedDiff.append(buildDiff(filename, expected, actual));
            }
        }

        // Check for undeclared files produced by the container.
        for (String filename : new TreeMap<>(producedFiles).keySet()) {
            if (!expectedFiles.containsKey(filename)) {
                combinedDiff.append(buildDiff(filename, "", normalize(producedFiles.get(filename))));
            }
        }

        if (!combinedDiff.isEmpty()) {
            return EvaluationResult.fail(combinedDiff.toString());
        }

        return EvaluationResult.pass();
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

    private static String buildDiff(String filename, String expected, String actual) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- expected/").append(filename).append('\n');
        sb.append("+++ actual/").append(filename).append('\n');

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
