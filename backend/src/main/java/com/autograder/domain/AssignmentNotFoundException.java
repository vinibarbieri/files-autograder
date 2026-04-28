package com.autograder.domain;

/**
 * Thrown when an {@link AssignmentRepository} lookup finds no assignment
 * for the requested id.
 *
 * <p>Carries the id that was looked up so the web layer can produce a precise
 * 404 problem-detail response without string parsing (cycle 9).
 */
public class AssignmentNotFoundException extends RuntimeException {

    private final String assignmentId;

    /**
     * @param assignmentId the id that was not found (included in the message)
     */
    public AssignmentNotFoundException(String assignmentId) {
        super("Assignment not found: " + assignmentId);
        this.assignmentId = assignmentId;
    }

    /** @return the assignment id that triggered this exception */
    public String assignmentId() {
        return assignmentId;
    }
}
