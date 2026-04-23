# Autograder Frontend

Placeholder for the React + Vite SPA. Deliberately not scaffolded in the foundation phase — Milestone 1 focuses on the backend pipeline first.

## Planned Scope (Milestone 1)

A single page with:

- Assignment picker (dropdown)
- `.c` file upload
- Submit button
- Result panel (compile log, diff, pass/fail)

No auth, no routing, no state management library. Hit `POST /api/submissions` and render the response.

## When You Scaffold

```bash
npm create vite@latest . -- --template react-ts
npm install
npm run dev
```

Target API base URL is configurable via `VITE_API_BASE_URL` (defaults to `http://localhost:8080`).

## Conventions (agreed in advance)

- TypeScript
- Component files: `PascalCase.tsx`
- One component per file
- No CSS frameworks for Milestone 1 — plain CSS modules
- Testing: Vitest + React Testing Library (added when the first component lands)
