package com.autograder.application;

import com.autograder.domain.Assignment;
import com.autograder.domain.AssignmentRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * In-memory {@link AssignmentRepository} test double for use in
 * application-layer unit tests.
 *
 * <p>Seeded at construction via varargs; additional assignments can be added
 * after construction with {@link #store(Assignment)}. Not thread-safe — test
 * use only.
 */
public class InMemoryAssignmentRepository implements AssignmentRepository {

    private final Map<String, Assignment> store = new HashMap<>();

    /** Constructs an empty repository. */
    public InMemoryAssignmentRepository() {
    }

    /**
     * Constructs a repository pre-seeded with the given assignments.
     *
     * @param assignments zero or more assignments to store; keyed by {@link Assignment#id()}
     */
    public InMemoryAssignmentRepository(Assignment... assignments) {
        for (Assignment a : assignments) {
            store(a);
        }
    }

    /**
     * Adds or replaces an assignment. Useful for seeding state between test
     * arrange and act phases.
     *
     * @param assignment the assignment to store (non-null)
     */
    public void store(Assignment assignment) {
        Objects.requireNonNull(assignment, "assignment");
        store.put(assignment.id(), assignment);
    }

    @Override
    public Optional<Assignment> findById(String id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(store.get(id));
    }
}
