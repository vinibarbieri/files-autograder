# ADR-0006: Load assignments + answer keys from a private git repo

- **Status:** Accepted
- **Date:** 2026-04-22
- **Deciders:** Vinicius Barbieri

## Context

Assignments consist of: a manifest (compile flags, entry point, timeout, evaluation strategy), one or more input files, and expected outputs. They change over the semester, and the expected outputs are sensitive — a student with read access to the answer key trivially "passes" every run.

We considered three sourcing options:

1. **Filesystem-only** — drop files in a folder on the host. Simple; awkward to version or update remotely.
2. **Bundled in the JAR** — `src/main/resources/assignments/`. Simplest for local dev, but requires a redeploy for every update.
3. **Private git repo** — the backend pulls from a git URL at startup (and on demand).

## Decision

Assignments are stored in a **separate private git repository** owned by the instructor. The backend clones/pulls this repo into a local cache directory (`autograder.assignments.cache-dir`) at startup and whenever an explicit refresh is requested. The backend **never** serves raw expected-output files over HTTP — they are read only by the evaluation strategy.

## Consequences

### Positive
- Assignments are versioned, reviewable, and diff-friendly.
- Instructors update assignments without touching the backend deploy.
- Clean separation of concerns: the autograder is just a runtime; the *what-is-graded* lives elsewhere.
- Sensitive answer keys stay out of the public autograder repository.

### Negative / Trade-offs
- The backend now has a hard dependency on git (binary + credentials). Mitigated by using JGit embedded in the JVM.
- A failed pull must not wipe the last-known-good cache. The refresh flow must be transactional: clone into a staging dir, swap on success.
- The `assignments-source-repo-url` and credentials are now secrets the deployment must manage.

### Follow-ups
- Credential strategy (SSH deploy key vs. token) picked in a deployment ADR.
- Manifest schema (`assignment.yaml`) specified in `docs/TDD_ROADMAP.md` and locked via a schema test.
