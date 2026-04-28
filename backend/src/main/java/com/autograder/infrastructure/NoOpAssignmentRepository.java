package com.autograder.infrastructure;

import com.autograder.domain.Assignment;
import com.autograder.domain.AssignmentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Placeholder {@link AssignmentRepository} that satisfies the Spring context
 * until {@code GitAssignmentRepository} is implemented in cycle 11.
 *
 * <p>Registered only when no other {@link AssignmentRepository} bean is present
 * ({@code @ConditionalOnMissingBean}), so it is automatically superseded when
 * the real adapter is wired.
 *
 * <p>Returns {@link Optional#empty()} for every lookup, which surfaces as an
 * {@code AssignmentNotFoundException} in the service layer.
 */
@Component
@ConditionalOnMissingBean(AssignmentRepository.class)
class NoOpAssignmentRepository implements AssignmentRepository {

    @Override
    public Optional<Assignment> findById(String id) {
        return Optional.empty();
    }
}
