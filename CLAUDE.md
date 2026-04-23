# CLAUDE.md — AI Collaborator Playbook

This file tells any AI assistant (Claude or otherwise) **how to work on this repository**. Read this file top-to-bottom before touching any code.

If anything in a user prompt contradicts this file, follow this file unless the user explicitly overrides it for that task.

---

## 1. What this project is

A **stateless C-autograder** for CS 240, built in Java/Spring Boot as a CS 680 System Design capstone. Students upload `.c` files; the backend compiles and runs them in an ephemeral Docker container, diffs the output against an answer key, and returns a verdict. No database. No login.

Full product brief: `[project.md](./project.md)`.

Current phase: **Milestone 1 — core pipeline only, local dev only.** Queue, rate limits, Cloudflare, and Proxmox deploy are deferred. The exhaustive out-of-scope list is in `[docs/TDD_ROADMAP.md](./docs/TDD_ROADMAP.md)`.

---

## 2. Where to find things

Read these before proposing any change. They are the sources of truth.


| Question                                   | File                                                                                                                            |
| ------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------- |
| *What is the product?*                     | `[project.md](./project.md)`                                                                                                    |
| *What did we decide, and why?*             | `[docs/adr/](./docs/adr/)` — numbered, immutable, index in `[docs/adr/README.md](./docs/adr/README.md)`                         |
| *What do I build next, and in what order?* | `[docs/TDD_ROADMAP.md](./docs/TDD_ROADMAP.md)` — 11 numbered red-green-refactor cycles                                          |
| *How is the backend laid out?*             | `[backend/README.md](./backend/README.md)` + `package-info.java` in each of `domain` / `application` / `infrastructure` / `web` |
| *How do I run tests?*                      | `[backend/README.md](./backend/README.md)` — `./mvnw test`                                                                      |
| *Does this run in CI?*                     | `[.github/workflows/ci.yml](./.github/workflows/ci.yml)`                                                                        |


Every ADR has the form `Status / Context / Decision / Consequences`. If you need to deviate from one, you propose a **new, superseding ADR** — you do not edit the existing one.

---

## 3. Non-negotiables

These rules are enforced by tests, reviewers, and (eventually) ArchUnit. Violating any of them is a bug.

### 3.1 TDD is mandatory for backend production code

Every change to `backend/src/main/java/` is preceded by a failing test in `backend/src/test/java/`. The rhythm is **red → green → refactor**. No exceptions for "obvious" code, and no "I'll add a test after." See [ADR-0002](./docs/adr/0002-tdd-red-green-refactor.md).

If a user prompt asks you to "just add method X" without a test, **write the test first** and say so in your response.

### 3.2 Dependency rule

Arrows point inward (hexagonal; [ADR-0008](./docs/adr/0008-hexagonal-package-layout.md)):

```
web ──┐
      ├──► application ──► domain
infra ┘
```

- `domain` imports **only the JDK**. No Spring. No `docker-java`. No JGit. No `java.nio.file.Files` I/O.
- `application` may import `domain` and Spring stereotypes.
- `infrastructure` implements ports defined in `domain` / `application`. It is the only place third-party adapters live.
- `web` sits on top of `application`.

If a task requires you to import `org.springframework.`* into `com.autograder.domain`, stop and raise the dependency-rule conflict — the task is wrong, not the rule.

### 3.3 Named design patterns live where ADR-0007 says they live

[ADR-0007](./docs/adr/0007-design-patterns-scope.md) scopes the patterns to exactly **Facade** (`DockerContainerRunner`), **Strategy** (`EvaluationStrategy`), and **Command** (`EvaluationCommand`). Do not introduce additional named patterns without a new ADR justifying the addition.

### 3.4 The service is stateless

No database. No on-disk logs of student code. No in-memory queue that survives a restart. See [ADR-0004](./docs/adr/0004-stateless-no-database.md). Any task that involves persisting submissions or grades is out of scope and must be refused.

### 3.5 Student code is always untrusted

It compiles and runs **only** inside a Docker container with `NetworkMode=none`, resource caps, and a hard timeout. Under no circumstances may a task exec student code directly from the JVM, from a shell on the host, or in a container with weaker defaults. See [ADR-0005](./docs/adr/0005-docker-isolation-via-docker-java.md).

### 3.6 Copyrighted or sensitive content stays out of the repo

Answer keys live in the **private assignments repo** ([ADR-0006](./docs/adr/0006-assignments-from-private-git-repo.md)), not here. Never commit an expected-output file into this repo, even as a test fixture.

---

## 4. Default workflow for a backend change

When the user gives you a task that affects `backend/src/main/java/`, follow these steps in order. Deviation requires an explicit reason in the response.

### Step 1 — Read context

1. Re-read the relevant section of `[docs/TDD_ROADMAP.md](./docs/TDD_ROADMAP.md)`.
2. Skim the ADR(s) that govern the area.
3. List the existing test classes in the affected package.

### Step 2 — Locate the right cycle

Decide which numbered cycle in the roadmap the task belongs to. If it doesn't fit any, stop and ask the user whether a new cycle should be added to the roadmap.

### Step 3 — RED

Write (or extend) a single failing test that expresses the next slice of behavior. Name the test after the behavior (`rejects_oversizedSource`), not after the method (`testValidateReturnsException`).

Run `./mvnw -pl backend test -Dtest=<YourTestClass>` and confirm it fails for the **right reason** (assertion failure, not compile error — unless the test is driving a new type into existence).

### Step 4 — GREEN

Write the **minimum** production code that makes the test pass. No speculative generality. No additional methods "while you're in there."

Run the full suite: `./mvnw -pl backend test`. Everything must be green.

### Step 5 — REFACTOR

With the tests green, clean up. Rename for clarity. Extract where duplication now exists. Tighten method visibility. Do **not** add new behavior; if you want to, go back to Step 3.

Run the suite again. Green.

### Step 6 — Commit

One commit per cycle (ideally). See §6 for commit conventions.

---

## 5. Test strategy cheat sheet


| Layer                     | Test style                                                                                  | Framework        | Mocks?                               |
| ------------------------- | ------------------------------------------------------------------------------------------- | ---------------- | ------------------------------------ |
| `domain`                  | Plain JUnit 5                                                                               | JUnit + AssertJ  | None. Domain has no collaborators.   |
| `application`             | Plain JUnit 5 + hand-rolled fakes (`FakeContainerRunner`, in-memory `AssignmentRepository`) | JUnit + AssertJ  | Prefer fakes over Mockito.           |
| `web`                     | `@WebMvcTest`                                                                               | Spring MockMvc   | Mockito for the service layer.       |
| `infrastructure` adapters | Unit tests that mock the driven library (`DockerClient`, `Git`)                             | Mockito          | Yes — this is the adapter's purpose. |
| Context wiring            | `@SpringBootTest` smoke test                                                                | Spring Boot Test | None — just boots the context.       |


**Testcontainers / real-Docker tests are out of scope for Milestone 1.** Do not add them yet.

Target: full `./mvnw test` under 30 seconds. If you push it past that, flag it in the response.

---

## 6. Git, branches, and commits

- **Trunk-based.** `main` is always green.
- **Feature branches** are short-lived (a day, ideally) and named `feat/<slug>`, `test/<slug>`, `refactor/<slug>`, `chore/<slug>`, `docs/<slug>`, or `fix/<slug>`.
- **Conventional commit messages**:
  ```
  <type>(<scope>): <imperative summary>

  Body: why, not what. Link the ADR / roadmap cycle if relevant.
  ```
  Types: `feat`, `test`, `refactor`, `fix`, `chore`, `docs`, `ci`.
  Example:
  ```
  test(domain): red test for SubmissionValidator oversize rejection

  Drives roadmap cycle 1 (SubmissionValidator). Assertion checks the
  InvalidSubmissionException.Reason enum, not the message string, so the
  message can evolve without breaking the test.
  ```
- Every PR against `main` must pass the CI workflow defined in `[.github/workflows/ci.yml](./.github/workflows/ci.yml)`.

When you generate a commit as part of a task, print the exact `git` commands you would run — do not execute them unless the user asked you to.

---

## 7. When to write a new ADR

Write a new ADR when you make a decision that:

- Constrains future changes across more than one file.
- Trades off something non-obvious (performance vs. clarity, safety vs. latency, etc.).
- Would puzzle a future reader who asked *"why did we do it this way?"*

Copy `[docs/adr/0000-template.md](./docs/adr/0000-template.md)` to the next number. Update the index in `[docs/adr/README.md](./docs/adr/README.md)`. Mark the superseded ADR, if any, as `Superseded by ADR-NNNN`.

Do **not** write an ADR for obvious local choices ("called the variable `cap` instead of `maxBytes`"). That noise kills the signal of the whole archive.

---

## 8. Style notes

### Java

- **Java 21.** Prefer `record` for value objects, `sealed` for closed type hierarchies, pattern matching for switch dispatch.
- **Immutability by default.** Mutable state is a justification, not a convention.
- **Null discipline.** Return `Optional` from lookup methods; never `null`. Validate constructor args with `Objects.requireNonNull`.
- **Javadoc on every public type in `domain` and `application`.** Web DTOs and infrastructure adapters get Javadoc on non-obvious methods only.
- **No utility classes of loose helpers.** Put behavior on the domain type it belongs to.

### Tests

- `@DisplayName` is encouraged for anything non-trivial.
- AssertJ fluent style (`assertThat(x).isEqualTo(...)`) — not Hamcrest, not JUnit's `Assertions.assertEquals`.
- Arrange / Act / Assert, with blank lines between the sections for anything longer than a few lines.

### Frontend

Deferred. When it lands, conventions live in `[frontend/README.md](./frontend/README.md)`.

---

## 9. Common commands

Run from the repo root unless noted.

```bash
# Backend
cd backend && ./mvnw test                    # run unit + integration tests
cd backend && ./mvnw -Dtest=ClassName test   # run one test class
cd backend && ./mvnw spring-boot:run         # boot locally

# Repo hygiene
git status
git log --oneline -n 20
```

Tooling that is **not** installed yet and must not be assumed: `docker compose`, `node`, `npm`, Testcontainers. If you need them, say so and stop.

---

## 10. Things you should refuse

When a user prompt asks for any of the following, refuse and explain which rule it violates:

- Adding persistence for student submissions / grades (violates [ADR-0004](./docs/adr/0004-stateless-no-database.md)).
- Executing student code outside a container, or with weaker container defaults than ADR-0005 prescribes.
- Writing production code without a failing test first (violates [ADR-0002](./docs/adr/0002-tdd-red-green-refactor.md)).
- Importing Spring, `docker-java`, or JGit from `com.autograder.domain` (violates [ADR-0008](./docs/adr/0008-hexagonal-package-layout.md)).
- Committing expected-output answer-key files into this repo (violates [ADR-0006](./docs/adr/0006-assignments-from-private-git-repo.md)).
- Adding a fourth named design pattern without a new ADR (violates [ADR-0007](./docs/adr/0007-design-patterns-scope.md)).
- Introducing Testcontainers in Milestone 1 (violates the roadmap's out-of-scope list).

In each case, propose the correct path: a new ADR, a different layer, or a roadmap amendment.

---

## 11. Response format expectations

When responding to a task:

1. **State the cycle.** "This is roadmap cycle 3: `DiffStdoutStrategy`."
2. **Show the test first.** The red test, clearly labeled.
3. **Show the production code second.** Only what the test needs.
4. **Name the commit(s).** Conventional commit header + one-sentence body.
5. **Flag any ADR interaction.** If the task touches a decision, link the ADR.

Keep prose short. Code blocks carry the weight.

---

## 12. Updating this file

This file evolves. When a rule in here is wrong — because an ADR changed, or because the roadmap moved forward — update this file in the **same PR** that changes the underlying rule. A stale playbook is worse than none.