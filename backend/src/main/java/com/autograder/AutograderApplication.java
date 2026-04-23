package com.autograder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the stateless C-autograder backend.
 *
 * <p>Application wiring stays intentionally thin: controllers delegate to
 * application services, which in turn depend on domain interfaces. Docker and
 * filesystem adapters live in {@code com.autograder.infrastructure} and are
 * wired via Spring configuration so they can be replaced with test doubles.
 */
@SpringBootApplication
public class AutograderApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutograderApplication.class, args);
    }
}
