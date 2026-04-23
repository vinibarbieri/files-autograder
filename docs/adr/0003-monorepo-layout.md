# ADR-0003: Single monorepo with `backend/` + `frontend/`

- **Status:** Accepted
- **Date:** 2026-04-22
- **Deciders:** Vinicius Barbieri

## Context

Backend (Java / Spring Boot) and frontend (React / Vite) ship together and share a trivially small API surface. The project is a solo effort with no organizational reason to separate ownership.

## Decision

We keep both in a **single git repository** with top-level `backend/` and `frontend/` folders, plus shared `docs/` and `.github/workflows/`.

## Consequences

### Positive
- Atomic commits can touch the API contract on both sides.
- One CI pipeline, one issue tracker, one README entry point.
- ADRs live alongside the code they describe.

### Negative / Trade-offs
- The CI workflow must partition build/test steps per folder to keep runtime bounded. Mitigated with path filters in the GitHub Actions triggers.
- Mixing language tooling at the root (`pom.xml` vs. `package.json`) means root-level scripts must be avoided; each stack stays inside its own folder.

### Follow-ups
- If the frontend is ever deployed independently on a different cadence, revisit this decision rather than introduce brittle release coordination.
