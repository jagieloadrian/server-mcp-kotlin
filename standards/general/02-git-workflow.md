# Git and Workflow

## Branches

- Work branches: `feature-<n>/<short-kebab-name>` from `main`, for example `feature-9/broker-coroutines`.
- Spec-kit features use the same `feature-<n>/` branches, not spec-kit's `<NNN>-<short-name>`.
- PRs are squash-merged into `main`. The PR title follows Conventional Commits, because it becomes the commit
  subject on `main`.
- For parallel work, use a separate `git worktree` instead of switching a checkout that has uncommitted changes.

## Commits

- Conventional Commits: `type(scope): imperative summary`. Types: `feat`, `fix`, `refactor`, `test`, `docs`,
  `build`, `ci`, `chore`.
- One atomic commit per unit of work. A refactor, a fix and its docs are separate commits.
- The body explains why, in at most 120 characters in total. It replaces code comments.
- Stage files by name. No `git add -A` or `git add .`.
- Never put `[skip ci]`, `[ci skip]`, `[no ci]`, `[skip actions]` or `skip-checks: true` in a commit message or
  PR text, not even as a description. GitHub then skips every workflow for the PR and its squash merge. Only the
  CI tag-bump commit carries such a marker.
- Never commit secrets or unencrypted secret files.
- No AI attribution in commits or PRs: no `Co-Authored-By` trailer (for any agent or person), no "Generated
  with …" line. The commit carries only the author's own identity.
- When starting in a new project, check that the agent's attribution setting is off (Claude Code:
  `attribution.commit` and `attribution.pr` set to `""` in `.claude/settings.json`).

```text
refactor(broker): run the consumer as a coroutine instead of a raw thread

The consumer loop runs in its own coroutine on Dispatchers.IO.
This drops the runBlocking bridge.
```

## Before every commit

1. The change is complete.
2. Tests covering the touched code are green.
3. Project gates pass (for Kotlin: `./gradlew check build` with ktlint, tests, coverage).
4. The built behaviour matches each acceptance criterion.
5. Fix any failure or gap, then run the checks again.

## Pushing

- Push only when asked.
- Never push straight to `main`. Changes reach `main` through a PR.
- Do not push leftover agent or worktree branches.

## Other people's work

- Never commit, revert or stash someone else's uncommitted changes.
- Never use a bare `git stash`. The stash stack is shared between worktrees and sessions.

## PROJECT-STATUS.md

Update it at the end of each session:

- Completed work, with absolute dates and branch names.
- Important decisions and their reasons, including what was left out on purpose.
- Next steps and open risks.
