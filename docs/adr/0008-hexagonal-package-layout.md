# ADR-0008: Hexagonal package layout (domain / application / infrastructure / web)

- **Status:** Accepted
- **Date:** 2026-04-22
- **Deciders:** Vinicius Barbieri

## Context

A flat package structure ("put everything under `com.autograder`") would make the CS 680 design-patterns story muddled: where does the Strategy interface live? Where do adapters plug in? Where does Spring end and the domain begin?

We want a layout that:

- Makes dependency direction obvious and enforceable.
- Lets the domain stay framework-free (the "pure Java" core).
- Gives us a stable home for each pattern in ADR-0007.

## Decision

We use a **four-package hexagonal-inspired layout** under `com.autograder`:

| Package | Contents | Allowed imports |
|---------|----------|-----------------|
| `domain` | Entities, value objects, ports (interfaces), domain exceptions. `EvaluationStrategy` interface lives here. | JDK only |
| `application` | Use-case services that orchestrate domain + ports. `EvaluateSubmissionService`, `EvaluationCommand`. | `domain`, Spring stereotypes (`@Service`) |
| `infrastructure` | Adapters implementing ports: `DockerContainerRunner` (Facade), `GitAssignmentRepository`, file-based caches. | `domain`, `application`, third-party libs |
| `web` | REST controllers, DTOs, exception handlers, OpenAPI wiring. | `application`, `domain`, Spring Web |

**Dependency rule:** arrows point inward. `domain` knows nothing. `application` depends on `domain`. `infrastructure` and `web` depend on `application` and `domain`. Nothing outside `domain` is allowed to reach into another outer package.

## Consequences

### Positive
- The domain can be unit-tested with pure JUnit — fast, no Spring context.
- Each pattern has an obvious home: Facade in `infrastructure`, Strategy interface in `domain` + implementations in `application` or `infrastructure`, Command in `application`.
- When the queue arrives in Milestone 2, it slots into `application` without touching `domain` or controllers.

### Negative / Trade-offs
- Slightly more ceremony than a single-package app for such a small service.
- Contributors must learn the dependency rule. Mitigated by an ArchUnit test (Milestone 2) that enforces it automatically.

### Follow-ups
- Add an ArchUnit test in Milestone 2 to guarantee no `domain → infrastructure` imports sneak in.
