package com.autograder.application;

import com.autograder.domain.Assignment;
import com.autograder.domain.AssignmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RED tests for roadmap cycle 5: {@link AssignmentRepository} port +
 * {@link InMemoryAssignmentRepository} test double.
 *
 * <p>All assertions are made through the {@code AssignmentRepository} interface
 * so that the port contract is what is verified, not the implementation detail.
 */
class InMemoryAssignmentRepositoryTest {

    @Test
    @DisplayName("findById returns the stored assignment when present")
    void findById_returns_stored_assignment() {
        Assignment hw01 = assignment("hw01");
        AssignmentRepository repo = new InMemoryAssignmentRepository(hw01);

        assertThat(repo.findById("hw01")).contains(hw01);
    }

    @Test
    @DisplayName("findById returns empty for an unknown id")
    void findById_returns_empty_for_unknown_id() {
        AssignmentRepository repo = new InMemoryAssignmentRepository();

        assertThat(repo.findById("unknown")).isEmpty();
    }

    @Test
    @DisplayName("findById returns empty when id differs by case")
    void findById_is_case_sensitive() {
        AssignmentRepository repo = new InMemoryAssignmentRepository(assignment("hw01"));

        assertThat(repo.findById("HW01")).isEmpty();
    }

    @Test
    @DisplayName("multiple assignments are stored independently")
    void multiple_assignments_stored_independently() {
        Assignment hw01 = assignment("hw01");
        Assignment hw02 = assignment("hw02");
        AssignmentRepository repo = new InMemoryAssignmentRepository(hw01, hw02);

        assertThat(repo.findById("hw01")).contains(hw01);
        assertThat(repo.findById("hw02")).contains(hw02);
    }

    @Test
    @DisplayName("store() adds an assignment after construction")
    void store_adds_assignment_after_construction() {
        InMemoryAssignmentRepository repo = new InMemoryAssignmentRepository();
        Assignment hw03 = assignment("hw03");

        repo.store(hw03);

        assertThat(repo.findById("hw03")).contains(hw03);
    }

    @Test
    @DisplayName("store() overwrites an existing assignment with the same id")
    void store_overwrites_existing_assignment() {
        Assignment original = assignment("hw01");
        Assignment updated  = assignment("hw01");
        InMemoryAssignmentRepository repo = new InMemoryAssignmentRepository(original);

        repo.store(updated);

        assertThat(repo.findById("hw01")).contains(updated);
    }

    // ---------- helper ----------

    private static Assignment assignment(String id) {
        return new Assignment(
                id,
                List.of(),
                Duration.ofSeconds(10),
                List.of(),
                "",
                Map.of(),
                "diff-stdout");
    }
}
