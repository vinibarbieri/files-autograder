package com.autograder.infrastructure;

import com.autograder.domain.Assignment;
import com.autograder.domain.ContainerRunner;
import com.autograder.domain.ExecutionResult;
import com.autograder.domain.Submission;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Placeholder {@link ContainerRunner} that satisfies the Spring context until
 * {@code DockerContainerRunner} is implemented in cycle 10.
 *
 * <p>Registered only when no other {@link ContainerRunner} bean is present
 * ({@code @ConditionalOnMissingBean}), so it is automatically superseded when
 * the real adapter is wired.
 *
 * <p><b>Not for production use.</b> Throws {@link UnsupportedOperationException}
 * if called at runtime.
 */
@Component
@ConditionalOnMissingBean(ContainerRunner.class)
class NoOpContainerRunner implements ContainerRunner {

    @Override
    public ExecutionResult run(Submission submission, Assignment assignment) {
        throw new UnsupportedOperationException(
                "NoOpContainerRunner is a placeholder — wire DockerContainerRunner (cycle 10).");
    }
}
