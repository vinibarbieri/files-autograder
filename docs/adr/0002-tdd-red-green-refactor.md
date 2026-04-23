# ADR-0002: Drive backend work with TDD

- **Status:** Accepted
- **Date:** 2026-04-22
- **Deciders:** Vinicius Barbieri

## Context

Most of the production code in this project will be written with an LLM-in-the-loop. Without an external signal anchoring each change, it is easy to drift — the model will happily write plausible code that subtly violates intent.

Tests written first provide that signal. They express intent in an executable form, fail immediately when the model over-reaches, and double as living documentation of behavior.

## Decision

We use **Test-Driven Development** for all backend production code. Every change follows the **red → green → refactor** cycle:

1. **Red** — write (or extend) a failing test that describes the next slice of behavior.
2. **Green** — write the minimum production code required to make it pass.
3. **Refactor** — improve the design with the test suite as a safety net.

The test stack is **JUnit 5 + Mockito + AssertJ + Spring Boot Test**. Unit tests and integration tests are in scope; Testcontainers-based end-to-end tests are explicitly **out of scope for Milestone 1** (see Follow-ups).

## Consequences

### Positive
- Each commit shows both an intent (the test) and its fulfillment (the code), making LLM-assisted work auditable.
- Refactors remain safe as the pattern-heavy backend evolves.
- Integration tests at the Spring boundary validate wiring without launching Docker.

### Negative / Trade-offs
- Mocking the Docker adapter means we do not exercise real container lifecycle until Milestone 2+. Contract tests on the adapter interface must stay tight to prevent drift.
- Initial velocity is lower than "just write it"; this is accepted as the price of control.

### Follow-ups
- When the queue lands (Milestone 2), introduce Testcontainers E2E tests that spin up a real GCC image.
- Target: keep the unit test suite under 30 seconds.
