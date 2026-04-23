# ADR-0001: Use Spring Boot as the backend framework

- **Status:** Accepted
- **Date:** 2026-04-22
- **Deciders:** Vinicius Barbieri

## Context

The backend must expose a small HTTP API (single upload endpoint in Milestone 1), orchestrate Docker containers, and be developed test-first. It will also serve as the vehicle for demonstrating object-oriented design patterns for the CS 680 System Design course.

Two realistic options were considered:

1. **Spring Boot** — batteries-included framework with strong DI, first-class testing story (`@SpringBootTest`, `@WebMvcTest`), mature ecosystem, and an idiomatic place to wire design patterns via `@Service` / `@Component` beans.
2. **Javalin** — minimalist web framework; leaves the wiring to the developer, which makes design patterns more explicit but costs more infrastructure code.

## Decision

We use **Spring Boot 3.x on Java 21 with Maven**.

## Consequences

### Positive
- Dependency injection enables clean seams for test doubles; the Docker adapter can be swapped for a mock without custom plumbing.
- `spring-boot-starter-test` bundles JUnit 5, Mockito, AssertJ, and MockMvc — no further test stack decisions needed.
- Design patterns (Facade, Strategy, Command) map cleanly onto Spring bean lifecycles.
- Large talent pool and documentation reduces research overhead.

### Negative / Trade-offs
- Heavier startup time and larger jar compared to Javalin. Acceptable: the service is long-running on the home lab host.
- Framework magic can obscure wiring. Mitigated by restricting framework annotations to the `application` / `infrastructure` / `web` packages; `domain` stays pure Java.

### Follow-ups
- ADR-0008 pins the package layout that keeps Spring out of the domain.
- Deployment packaging (fat jar vs. native image) deferred to the hosting milestone.
