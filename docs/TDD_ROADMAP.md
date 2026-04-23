# TDD Roadmap — Milestone 1

This document is the working plan for the first red-green-refactor marathon. It names the components, orders them, and defines "done" for each. Every item below is a checkpoint in the test suite before it is a checkpoint in the production code.

## Principles

1. **Test the behavior, not the implementation.** A test is a specification, not a type lock-in.
2. **One red per commit.** A commit should land either a failing test, the code that makes it pass, or a refactor — not a mix.
3. **Mock only at the edge.** The Docker adapter is the only mock. Domain and application code is tested with real objects.
4. **The domain stays pure.** No Spring annotations, no `docker-java` imports, no filesystem access in `com.autograder.domain`.
5. **Tests run in under 30 seconds for Milestone 1.** If they don't, something is wrong.

## Domain Glossary

A shared vocabulary the code and tests must honor:

| Term | Meaning |
|------|---------|
| **Submission** | A single student attempt: `(assignmentId, sourceFile, submittedAt, clientIp)`. |
| **Assignment** | The instructor-owned spec: id, compile flags, timeout, inputs, expected outputs, evaluation strategy name. |
| **AssignmentManifest** | The on-disk representation of an Assignment (`assignment.yaml`). |
| **ExecutionResult** | Raw output from the container: exit code, stdout, stderr, produced files, duration, timed-out flag. |
| **EvaluationResult** | Domain-level verdict: `PASS / FAIL / COMPILE_ERROR / TIMEOUT / INTERNAL_ERROR`, with a human-readable diff and compile log. |
| **EvaluationStrategy** | A pluggable comparator that turns an `ExecutionResult` + `Assignment` into an `EvaluationResult`. |

## Component Map

Dependencies point downward. Lower = more concrete, more infrastructure.

```
                       SubmissionController (web)
                                 │
                  EvaluateSubmissionService (application)
                     │        │         │       │
           SubmissionValidator│  AssignmentRepository (port)
                     │        │
             EvaluationCommand│
                              │
                     ContainerRunner (port)
                              │
                        EvaluationStrategy (port)
                              │
                       ┌──────┴──────┐
                       │             │
               DiffFileStrategy  DiffStdoutStrategy
                              │
              DockerContainerRunner (adapter, infrastructure)
              GitAssignmentRepository (adapter, infrastructure)
```

## Red-Green-Refactor Order

Each step is a full TDD cycle. Move to the next only when the previous is green **and** refactored.

### 1. `SubmissionValidator` (domain)
Validates the incoming submission *before* any I/O. Pure function; no mocks.

Tests:
- Rejects null / empty source.
- Rejects source larger than `max-source-size-bytes`.
- Rejects non-`.c` extensions.
- Rejects unknown `assignmentId` format (non-empty, alphanumeric + `_`/`-`, max length).
- Accepts a well-formed submission.

Definition of done: 100% branch coverage; no dependencies.

### 2. `EvaluationResult` value object (domain)
Immutable record(s) for the verdict. Tests drive the factory methods: `pass(...)`, `compileError(...)`, `fail(diff)`, `timeout(...)`, `internalError(...)`.

### 3. `EvaluationStrategy` contract + `DiffStdoutStrategy` (domain + application)
- Interface in `domain`.
- First concrete implementation in `application`.
- Tests feed crafted `ExecutionResult` + `Assignment` pairs, assert the `EvaluationResult`.
- Edge cases: trailing newline differences, CRLF vs LF, empty expected.

### 4. `DiffFileStrategy` (application)
Same contract as #3. Tests drive file-name resolution (the strategy only sees the in-memory `ExecutionResult.producedFiles` map — no filesystem access).

### 5. `AssignmentRepository` port + in-memory test double
Interface in `domain`. A hand-written in-memory double lives in `src/test/…` and powers later tests. The real `GitAssignmentRepository` does not exist yet.

### 6. `EvaluationCommand` (application, Command pattern)
Carries `Submission + Assignment`. `execute(ContainerRunner, EvaluationStrategy) → EvaluationResult`. Tests use stubbed `ContainerRunner` that returns canned `ExecutionResult`s to verify branching: compile error, timeout, strategy passes, strategy fails.

### 7. `ContainerRunner` port + fake in-memory implementation
Interface in `domain`. `FakeContainerRunner` in tests returns a script-configurable `ExecutionResult`. Used by #6 and later.

### 8. `EvaluateSubmissionService` (application)
Wires validator → repository → command → strategy. Tests use `@SpringBootTest` with the real wiring **but** the `ContainerRunner` bean is replaced with `FakeContainerRunner` (`@TestConfiguration`).

Key scenarios:
- Happy path (pass).
- Happy path (fail with diff).
- Unknown assignment → 404 domain exception.
- Invalid submission → validation exception.
- Timeout surfaces as `TIMEOUT` verdict.

### 9. `SubmissionController` (web)
`POST /api/submissions` (multipart). `@WebMvcTest` with a mocked service.

Tests:
- 200 with JSON body on success.
- 400 on missing file / wrong content-type / oversized upload.
- 404 when the service throws `AssignmentNotFoundException`.
- 500 collapsed into a generic problem-detail response.

### 10. `DockerContainerRunner` (infrastructure, Facade)
The only class that imports `docker-java`. Tested as a **unit** by mocking `DockerClient`:

- `createContainerCmd` called with the expected image, cmd, binds, memory, cpu, network.
- `startContainerCmd` then `waitContainerCmd` with the timeout.
- `removeContainerCmd` called in a `finally` block.
- Timeout path invokes `killContainerCmd` before removal.

A real-Docker test waits for Milestone 2 (Testcontainers).

### 11. `GitAssignmentRepository` (infrastructure)
JGit-backed. Tested against a local bare repository (not mocked):

- Clones into the cache dir on first call.
- `getById` returns a parsed `Assignment` from the manifest.
- `refresh()` pulls the latest commit.
- Failed refresh leaves the previous cache intact.

## Milestone 1 Acceptance Criteria

Milestone 1 is "done" when:

- [ ] All red-green-refactor cycles 1–11 are complete.
- [ ] `./mvnw test` is green, < 30 s, > 85% line coverage in `domain` + `application`.
- [ ] Running the backend with a local sample assignment repo and a sample `.c` file produces a correct `EvaluationResult` JSON response.
- [ ] Every ADR reference in the code is up to date (no dangling links).
- [ ] The README's "Quickstart" steps actually work on a clean clone.

## Explicitly Out of Scope for Milestone 1

- Processing queue and rate limiting (Milestone 2).
- Testcontainers / real-Docker integration tests (Milestone 2).
- Cloudflare Tunnel, Proxmox deployment (Milestone 3).
- Frontend wiring beyond the README placeholder.
- Observability: metrics, tracing, structured-log ingestion.
