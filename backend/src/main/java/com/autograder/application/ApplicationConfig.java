package com.autograder.application;

import com.autograder.domain.Assignment;
import com.autograder.domain.AssignmentRepository;
import com.autograder.domain.ContainerRunner;
import com.autograder.domain.ExecutionResult;
import com.autograder.domain.SubmissionPolicy;
import com.autograder.domain.Submission;
import com.autograder.domain.SubmissionValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Spring bean configuration for the application layer.
 *
 * <p>Provides {@link SubmissionPolicy} and {@link SubmissionValidator} as
 * managed beans. The policy uses {@link SubmissionPolicy#defaults()} for
 * Milestone 1; externalising it via {@code @ConfigurationProperties} is
 * deferred to the web layer (cycle 9 / Milestone 2).
 */
@Configuration
public class ApplicationConfig {

    /**
     * Provides the default submission policy for Milestone 1 local dev.
     * Replace with {@code @ConfigurationProperties} binding when external
     * configuration is required.
     */
    @Bean
    public SubmissionPolicy submissionPolicy() {
        return SubmissionPolicy.defaults();
    }

    /** Wires the validator with the configured policy. */
    @Bean
    public SubmissionValidator submissionValidator(SubmissionPolicy policy) {
        return new SubmissionValidator(policy);
    }

    /**
     * Placeholder repository bean so the Spring context can boot in cycle 8.
     * Real implementation lands in cycle 11.
     */
    @Bean
    public AssignmentRepository assignmentRepository() {
        return id -> Optional.empty();
    }

    /**
     * Placeholder runner bean so the Spring context can boot in cycle 8.
     * Real Docker adapter lands in cycle 10.
     */
    @Bean
    public ContainerRunner containerRunner() {
        return new ContainerRunner() {
            @Override
            public ExecutionResult run(Submission submission, Assignment assignment) {
                throw new UnsupportedOperationException(
                        "Placeholder ContainerRunner: DockerContainerRunner is implemented in cycle 10.");
            }
        };
    }
}
