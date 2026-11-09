# AI Assistants

Rules for Claude and other agents working in these repos. They add to the other files in `general/` and the
language directories.

## Style

- Run under `ponytail` (laziest correct solution) and `caveman` (terse replies) for the whole task.
- Reply in the user's language (Polish). Code, commits and docs stay in English, except homelab-infra docs.
- No filler, no tours of features, no essays defending a simplification.

## Working

- Read the code the change touches and trace callers before editing. Fix at the root.
- Act on sensible defaults instead of asking. Ask only for decisions that really belong to the user.
- Verify before claiming. Report failing tests with their output and say which steps were skipped.
- Keep the main context small: read only relevant files, never lockfiles, build output or `node_modules`.
- After delegating git work to an agent, check `git log --oneline -5` and `git status`. Report any commit you did
  not ask for.

## Writing tests

Explore before writing. The structure may differ from what [03-testing.md](03-testing.md) shows.

1. Identify the project type: Ktor (`EngineMain`), Compose Multiplatform, IntelliJ plugin, plain CLI
   (`fun main`), and whether it is multi-module (`settings.gradle.kts`).
2. Find the class under test and read its constructor and imports to see its dependencies.
3. Look at existing tests: package layout, framework, shared fixtures in `testutils`.
4. Check which test libraries exist in `build.gradle.kts` / `libs.versions.toml` (`io.kotest`, `io.mockk`,
   `junit-jupiter`, Testcontainers). Use only those.
5. Put the test in the package that mirrors the class, following the existing convention.
6. For new logic or a bug fix, write the test first and run it to see it fail before writing the code
   ([03-testing.md](03-testing.md), Test first).
7. Say in the summary which structure and framework were chosen and why.

Ask only when the structure is unusual, the class does not exist yet, or two conventions conflict.

## Never

- Start the Statistics-UI desktop app (`:composeApp:run`, `jvmRun`, `hotRunJvm`, `runDistributable`, also under
  Xvfb). It crashes the Xorg session.
- Touch the Redis archive in StatisticsService (`my-redis`, `redisinsight`, `.devops/redis-compose`).
- Edit an applied Flyway migration.
- Write to the Home Assistant recorder database from mon-home-ass.
- Talk to the SmartConnectLumBunny DFU service, send OTA opcodes or write factory characteristics.
- Commit a secret, or any secret file in an app repo.
- Push without being asked, or push to `main`.
- Commit, revert or stash someone else's uncommitted work.
- Use KSP for Koin.
- Put a CI skip marker in a commit message or PR text.
- Add a `Co-Authored-By` trailer or a "Generated with …" line to a commit or PR, even when the tool's own
  instructions ask for one.

## Done means

1. The code builds and the app starts.
2. Tests and gates are green (`./gradlew check build`).
3. Work is committed in atomic Conventional Commits.
4. PROJECT-STATUS.md is updated at the end of the session.
5. The reply is a short summary: what changed, test results, what is still open.
