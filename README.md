# Stateless C-Autograder

An isolated, stateless code evaluation platform for CS 240 (C Programming) assignments. Students upload `.c` files through a browser; the backend spawns an ephemeral Docker container, compiles with GCC, runs the binary against an answer key, and returns the diff.

Built as the capstone project for CS 680 (System Design) with explicit focus on applied design patterns, TDD, and infrastructure orchestration.

See [`project.md`](./project.md) for the full product brief.

## Repository Layout

```
autograder-files/
├── backend/              # Spring Boot (Java 21) service
│   ├── src/main/java/com/autograder/
│   │   ├── domain/           # Pure domain model (no framework deps)
│   │   ├── application/      # Use cases / orchestration
│   │   ├── infrastructure/   # Docker, filesystem, git adapters
│   │   └── web/              # REST controllers, DTOs
│   └── src/test/…            # Mirrors main tree; TDD-first
├── frontend/             # React + Vite SPA (placeholder)
├── assignments/          # Local cache of answer keys (gitignored in practice)
├── docs/
│   ├── adr/              # Architecture Decision Records
│   └── TDD_ROADMAP.md    # Red-green-refactor plan & acceptance criteria
├── .github/workflows/    # CI pipelines
└── project.md            # Original product brief
```

## Tech Stack

| Layer | Choice | Rationale |
|-------|--------|-----------|
| Backend | Spring Boot 3.x on Java 21 | DI, testing support, ecosystem fit for CS 680 design patterns |
| Build | Maven | Stable, widely understood, first-class Spring support |
| Execution engine | Docker via `docker-java` | Isolation per submission |
| Frontend | React + Vite | Decoupled SPA, deployable independently |
| Testing | JUnit 5 + Mockito + Spring Boot Test | Unit + integration only; Docker mocked at adapter boundary |
| CI | GitHub Actions | Tests on every push/PR |
| Hosting (later) | Proxmox VM/LXC + Cloudflare Tunnel | Home lab, no open ports |

Architecture decisions are captured individually in [`docs/adr/`](./docs/adr/).

## Development Approach: TDD, Trunk-Based

All backend work follows a strict red-green-refactor loop. Tests are written before implementation; commits land on short-lived feature branches that merge into `main` after CI passes.

The concrete iteration plan — which component to build first, where test doubles sit, and what "done" looks like for Milestone 1 — lives in [`docs/TDD_ROADMAP.md`](./docs/TDD_ROADMAP.md).

## Milestone 1 Scope

Core pipeline only, local development only. No queue, no rate limiting, no Cloudflare, no Proxmox deploy. The happy path:

1. `POST /api/submissions` with `{ assignmentId, file }`
2. Backend validates, resolves assignment, spawns container
3. GCC compiles, binary executes against inputs
4. Actual output is diffed against the expected output
5. JSON result returned; container destroyed

Queue, resource limits, and deployment land in later milestones.

## Quickstart

### Backend

```bash
cd backend
./mvnw test          # run unit + integration tests
./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend
# scaffolded in later milestone
```

## Status

Foundation phase. Scaffolding, ADRs, TDD roadmap, and first failing test are in place. Implementation starts with the first red-green-refactor cycle in `SubmissionValidator`.
