# Git: verify first, then one atomic conventional commit per unit of work

- Before committing: run tests covering touched code, check behavior against each requirement, re-run lint/CI gates. Fix any failure first.
- One atomic commit per unit of work.
- Conventional Commits: `feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`, `build:`.
- Never commit build artifacts, secrets, or generated files.
