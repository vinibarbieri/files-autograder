# Autograder Backend

Spring Boot 3.x service on Java 21. Receives C source submissions, dispatches them to ephemeral Docker containers, and returns diff-based evaluation results.

## Package Layout

Hexagonal-ish, four packages under `com.autograder`:

| Package | Responsibility | Framework deps allowed? |
|--------|----------------|-------------------------|
| `domain` | Value objects, entities, policy interfaces. The language of the problem. | No |
| `application` | Use-case services that orchestrate domain + ports (e.g. `EvaluateSubmissionService`). | No Spring in types; `@Service` allowed on classes |
| `infrastructure` | Adapters: docker-java, filesystem, git. Implement ports defined in `domain`/`application`. | Yes |
| `web` | REST controllers, DTOs, exception handlers. | Yes |

Test tree mirrors the main tree 1:1.

## Running

```bash
./mvnw test
./mvnw spring-boot:run
```

## Configuration

`application.yml` exposes keys under `autograder.*`. Override via environment variables at runtime, e.g. `AUTOGRADER_EXECUTION_MEMORY_LIMIT_MB=512`.

## Next Test to Write

Per the TDD roadmap, the current red-green-refactor target is `SubmissionValidator`. See [`../docs/TDD_ROADMAP.md`](../docs/TDD_ROADMAP.md).
