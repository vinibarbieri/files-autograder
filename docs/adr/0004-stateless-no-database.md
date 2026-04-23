# ADR-0004: No persistence — stateless per submission

- **Status:** Accepted
- **Date:** 2026-04-22
- **Deciders:** Vinicius Barbieri

## Context

The autograder is a pre-check tool. It is explicitly not the system of record for grades — that remains the course's official submission system. Storing student code or grades would introduce FERPA-like privacy obligations, attract attackers, and create a retention policy problem.

## Consequences of statefulness would include:
- A database to host, back up, and secure.
- A migration strategy.
- Data retention and deletion policy decisions.
- Increased attack surface for a service that compiles arbitrary third-party C code.

## Decision

The backend is **stateless**. No database, no user accounts, no persistent storage of submissions, logs, or results. Every request is independent; once the response is returned, the container and all artifacts are destroyed.

Assignments and answer keys are the *only* persistent state, and they flow **in** to the service — see ADR-0006.

## Consequences

### Positive
- Drastically reduced privacy and compliance risk.
- No backup, migration, or retention concerns.
- Horizontal scaling is trivial in future milestones.

### Negative / Trade-offs
- No history for students ("what did I submit last time?"). Out of scope by design.
- No server-side rate limiting based on identity; must be implemented at the edge (Cloudflare — later milestone).
- Observability is harder: without persistent logs the service is opaque post-hoc. Mitigated with structured stdout logging captured by the host.

### Follow-ups
- If operational telemetry proves insufficient, add aggregate metrics (counts only, no payloads) in a later ADR.
