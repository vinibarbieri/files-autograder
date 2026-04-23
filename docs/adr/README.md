# Architecture Decision Records

Short, immutable notes capturing *why* a non-obvious technical choice was made. New decisions are appended; old decisions are only ever **superseded**, never edited in place.

## Format

Each ADR follows Michael Nygard's lightweight template:

- **Status** — Proposed / Accepted / Superseded by ADR-NNNN
- **Context** — what forces are at play
- **Decision** — the choice we made
- **Consequences** — what becomes easier, what becomes harder

Use [`0000-template.md`](./0000-template.md) as a starting point.

## Index

| # | Title | Status |
|---|-------|--------|
| [0001](./0001-framework-spring-boot.md) | Use Spring Boot as the backend framework | Accepted |
| [0002](./0002-tdd-red-green-refactor.md) | Drive backend work with TDD | Accepted |
| [0003](./0003-monorepo-layout.md) | Single monorepo with `backend/` + `frontend/` | Accepted |
| [0004](./0004-stateless-no-database.md) | No persistence — stateless per submission | Accepted |
| [0005](./0005-docker-isolation-via-docker-java.md) | Isolate student code in ephemeral Docker containers | Accepted |
| [0006](./0006-assignments-from-private-git-repo.md) | Load assignments + answer keys from a private git repo | Accepted |
| [0007](./0007-design-patterns-scope.md) | Apply Facade, Strategy, and Command patterns explicitly | Accepted |
| [0008](./0008-hexagonal-package-layout.md) | Hexagonal package layout (domain / application / infrastructure / web) | Accepted |
