package com.autograder.application;

import com.autograder.domain.AssignmentNotFoundException;
import com.autograder.domain.Assignment;
import com.autograder.domain.ContainerRunner;
import com.autograder.domain.EvaluationResult;
import com.autograder.domain.EvaluationStrategy;
import com.autograder.domain.AssignmentRepository;
import com.autograder.domain.Submission;
import com.autograder.domain.SubmissionValidator;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Primary use-case service for evaluating a student submission.
 *
 * <p>Orchestrates the full pipeline in order:
 * <ol>
 *   <li>Validate the submission (rejects invalid input before any I/O).
 *   <li>Load the assignment from the repository (throws
 *       {@link AssignmentNotFoundException} if unknown).
 *   <li>Resolve the {@link EvaluationStrategy} by name from the assignment spec
 *       (returns {@link com.autograder.domain.Verdict#INTERNAL_ERROR} if the
 *       name is not registered).
 *   <li>Build and execute an {@link EvaluationCommand} with the resolved
 *       {@link ContainerRunner} and strategy.
 * </ol>
 *
 * <p>For Spring DI, the {@code strategies} map is auto-populated from all
 * {@link EvaluationStrategy} beans in the context, keyed by bean name. In
 * application-layer tests the map is supplied directly at construction.
 */
@Service
public class EvaluateSubmissionService {

    private final SubmissionValidator validator;
    private final AssignmentRepository repository;
    private final ContainerRunner runner;
    private final Map<String, EvaluationStrategy> strategies;

    /**
     * @param validator  validates submissions before any I/O
     * @param repository loads assignment specs by id
     * @param runner     compiles and runs student source in a container
     * @param strategies map of strategy-name → strategy; keyed by
     *                   {@link Assignment#evaluationStrategyName()}
     */
    public EvaluateSubmissionService(
            SubmissionValidator validator,
            AssignmentRepository repository,
            ContainerRunner runner,
            Map<String, EvaluationStrategy> strategies) {
        this.validator  = Objects.requireNonNull(validator,   "validator");
        this.repository = Objects.requireNonNull(repository,  "repository");
        this.runner     = Objects.requireNonNull(runner,      "runner");
        this.strategies = Map.copyOf(Objects.requireNonNull(strategies, "strategies"));
    }

    /**
     * Evaluates the submission end-to-end and returns a domain verdict.
     *
     * @param submission the student attempt (non-null)
     * @return the evaluation result; never {@code null}
     * @throws com.autograder.domain.InvalidSubmissionException if the
     *         submission fails pre-flight validation
     * @throws AssignmentNotFoundException if no assignment matches
     *         {@link Submission#assignmentId()}
     */
    public EvaluationResult evaluate(Submission submission) {
        Objects.requireNonNull(submission, "submission");

        validator.validate(submission);

        Assignment assignment = repository.findById(submission.assignmentId())
                .orElseThrow(() -> new AssignmentNotFoundException(submission.assignmentId()));

        EvaluationStrategy strategy = strategies.get(assignment.evaluationStrategyName());
        if (strategy == null) {
            return EvaluationResult.internalError(
                    "Unknown evaluation strategy: " + assignment.evaluationStrategyName());
        }

        EvaluationCommand command = new EvaluationCommand(submission, assignment);
        return command.execute(runner, strategy);
    }
}
