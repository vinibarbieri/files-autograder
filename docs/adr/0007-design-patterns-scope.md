# ADR-0007: Apply Facade, Strategy, and Command patterns explicitly

- **Status:** Accepted
- **Date:** 2026-04-22
- **Deciders:** Vinicius Barbieri

## Context

This project doubles as the deliverable for CS 680 (System Design). The course expects the student to motivate and apply canonical object-oriented design patterns. The risk is either "pattern soup" — adding patterns for their own sake — or pattern-by-accident where patterns are used but never named.

We pick a small, justified set and document where each one lives.

## Decision

The backend applies three named patterns, each for a concrete reason:

### Facade — `DockerContainerRunner`
`docker-java` exposes a sprawling API (image pulls, volume binds, cgroup knobs, log streaming, network modes). The service only needs three operations: `runSubmission(Spec) → ExecutionResult`, `pullImageIfMissing(name)`, and `healthcheck()`. A Facade on top of `DockerClient` collapses that surface, isolates the only test double in the system, and gives us one place to enforce container security defaults.

### Strategy — `EvaluationStrategy`
Different assignments are graded differently: some compare files byte-for-byte, some diff stdout, some match a regex, some need tolerance on floating-point output. An `EvaluationStrategy` interface lets each assignment specify its strategy at runtime via the assignment manifest, without the orchestrator knowing how comparisons work.

Initial implementations:
- `DiffFileStrategy` — unified `diff` over a named output file
- `DiffStdoutStrategy` — unified `diff` over captured stdout

### Command — `EvaluationCommand`
Each submission becomes a self-contained, executable `EvaluationCommand` object that carries its inputs (source code, assignment, client metadata) and produces a result when executed. Milestone 1 runs commands immediately; Milestone 2 will enqueue them into a bounded worker queue without changing the command shape — exactly what the pattern is for.

## Consequences

### Positive
- Each pattern maps to a named seam we can point to in the CS 680 defense.
- The Strategy and Command seams make it cheap to add new assignment types or move to async execution later.
- The Facade is the unit boundary that lets the rest of the backend be tested without Docker.

### Negative / Trade-offs
- Three patterns is the ceiling, not the floor. Adding a fourth needs a written justification in a new ADR.
- Over-abstracted Strategy interfaces are a classic anti-pattern; we accept the risk by starting with only two concrete strategies and letting the third arrive from a real requirement, not a speculative one.

### Follow-ups
- If queueing introduces a `CommandExecutor` / `CommandQueue` distinction, document it here (or supersede this ADR).
