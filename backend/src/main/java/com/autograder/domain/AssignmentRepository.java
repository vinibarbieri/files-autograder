package com.autograder.domain;

import java.util.Optional;

/**
 * Port for loading assignment specifications.
 *
 * <p>Implementations live in the {@code infrastructure} layer (e.g.
 * {@code GitAssignmentRepository}, cycle 11). A hand-rolled in-memory double
 * lives in {@code src/test/…} for use in application-layer tests.
 *
 * <p>Depends only on the JDK and other {@code domain} types (ADR-0008).
 */
public interface AssignmentRepository {

    /**
     * Returns the assignment with the given id, or {@link Optional#empty()} if
     * no assignment with that id is known.
     *
     * @param id the assignment identifier (non-null)
     * @return the matching assignment, or empty
     */
    Optional<Assignment> findById(String id);
}
