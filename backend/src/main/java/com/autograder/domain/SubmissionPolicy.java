package com.autograder.domain;

import java.util.List;
import java.util.Objects;

/**
 * Configuration knob bag for {@link SubmissionValidator}.
 *
 * <p>Lives in {@code domain} on purpose — the validator is a pure domain
 * concept, so its policy must not depend on Spring's {@code @ConfigurationProperties}.
 * The infrastructure layer is responsible for translating external config
 * into an instance of this record.
 *
 * @param maxSourceSizeBytes hard cap on the uploaded source size
 * @param allowedExtensions file extensions (including the leading dot) that are accepted
 * @param assignmentIdPattern regex the {@code assignmentId} must fully match
 */
public record SubmissionPolicy(
        int maxSourceSizeBytes,
        List<String> allowedExtensions,
        String assignmentIdPattern
) {

    public SubmissionPolicy {
        if (maxSourceSizeBytes <= 0) {
            throw new IllegalArgumentException("maxSourceSizeBytes must be positive");
        }
        Objects.requireNonNull(allowedExtensions, "allowedExtensions");
        if (allowedExtensions.isEmpty()) {
            throw new IllegalArgumentException("allowedExtensions must not be empty");
        }
        allowedExtensions = List.copyOf(allowedExtensions);
        Objects.requireNonNull(assignmentIdPattern, "assignmentIdPattern");
    }

    /**
     * @return a policy suitable for local development and tests
     */
    public static SubmissionPolicy defaults() {
        return new SubmissionPolicy(
                262_144,                  // 256 KiB
                List.of(".c"),
                "[A-Za-z0-9_-]{1,64}"
        );
    }
}
