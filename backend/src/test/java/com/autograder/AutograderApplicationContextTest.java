package com.autograder;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test: Spring context boots with the current wiring.
 *
 * <p>Kept deliberately bare. It only proves that the application assembles;
 * it does not assert any behavior. Each milestone adds a richer
 * integration test alongside this one.
 */
@SpringBootTest
class AutograderApplicationContextTest {

    @Test
    void contextLoads() {
        // No assertion: the test passes iff Spring can build the context.
    }
}
