# ADR-0005: Isolate student code in ephemeral Docker containers

- **Status:** Accepted
- **Date:** 2026-04-22
- **Deciders:** Vinicius Barbieri

## Context

We compile and execute untrusted third-party C code on our server. Running it in-process with the JVM or as a host-level subprocess is unacceptable — one `system("rm -rf /")`, one fork-bomb, or one pointer-abuse segfault must not compromise the host.

We need:

- A strong isolation boundary (filesystem, process tree, network).
- Enforceable resource limits (CPU, memory, wall-clock time).
- Fast spawn/destroy cycle.
- A Java client library with a clean API.

## Decision

Each submission runs in its own **ephemeral Docker container** based on an image that ships GCC (default: `gcc:14-bookworm`). The container is created, used, and removed inside a single request. The backend talks to the Docker daemon using the **`docker-java`** library.

The container starts with:

- Network disabled by default (`--network=none`).
- Memory cap (default 256 MiB).
- CPU quota.
- Read-only bind mount for source + inputs; writable tmpfs for build + run artifacts.
- Non-root user inside the container.
- A hard wall-clock kill enforced by the Java orchestrator (not just `--stop-timeout`).

## Consequences

### Positive
- Well-understood isolation primitive; no need to invent sandboxing.
- `docker-java` gives a Java-idiomatic API we can wrap behind a Facade (see ADR-0007).
- Container destruction guarantees no residual state, satisfying ADR-0004.

### Negative / Trade-offs
- Container startup adds ~200–500 ms per submission. Acceptable for pre-check use; unacceptable for a high-throughput grader (future problem).
- Requires a Docker daemon on the host; a `rootless` setup is preferred but not mandated yet.
- Docker socket access is equivalent to root on the host; only the backend user can access it.

### Follow-ups
- Investigate `podman` / rootless Docker in the deployment ADR.
- Investigate container pooling if startup latency becomes a bottleneck.
