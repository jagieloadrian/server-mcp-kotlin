# Project status

## Completed

### 2026-11-04, `main`
- stdio MCP server (Kotlin 2.4.21, `io.modelcontextprotocol:kotlin-sdk-server:0.16.0`, Gradle 9.8.2 wrapper).
- Tools: `list_standards` (name + first heading of each `standards/*.md`), `get_standard(name)`.
- Server `instructions` tell the client to check standards before writing, reviewing or committing code.
- Repo sync: fast-forward-only pull (KGit/JGit, `com.github.sya-ri:kgit:1.2.2`) of the repo containing the
  standards dir, at most every 24 h, triggered by a tool call. On failure it serves the local copy.
- Curated standards set `standards/01-…09-*.md` + `README.md` (index, precedence, known divergences).
- Rule against AI attribution in commits/PRs (`06-git-workflow.md`, `08-ai-assistants.md`).
- History rewritten without `Co-Authored-By` trailers (trees unchanged); branch renamed `master` → `main`.
- `~/.claude/settings.json`: `attribution` set to `{ "commit": "", "pr": "", "sessionUrl": false }`.

### 2026-11-04, `feature-1/apply-standards` (not merged)
- Code follows `02-kotlin.md`: no comments, `suspend fun main` instead of `runBlocking`, kotlin-logging with
  slf4j-simple, constants instead of magic strings, `internal` visibility, `CancellationException` rethrown.
- Server wiring extracted to `standardsServer(...)`; `main` only validates the argument and attaches stdio.
- Tests follow `05-testing.md`: JUnit 5, Kotest assertions, `runTest`, `should … when …`, given/when/then.
  14 tests: every sync path (pull, throttle, diverged history, unreachable remote, no git repo), listing,
  lookup, and all tools through an in-process MCP client.
- Gates in `./gradlew check build`: ktlint (`ktlint_official`), `allWarningsAsErrors`, JaCoCo line > 80 %,
  branch > 75 % (now 84 % / 79 %). Versions in `gradle/libs.versions.toml`, `renovate.json` added.

## Decisions
- stdio, no Ktor/HTTP: one user, local machine. Add HTTP only when a second machine or a team needs it.
- Files are read from disk on every call, so md edits need no rebuild.
- Pull is lazy (on a tool call, at most every 24 h). Add a background poller only if docs must refresh while idle.
- FF_ONLY + `setRebase(false)`: never merges, rebases or overwrites local edits (ignores `pull.rebase`).
  A diverged copy keeps serving local files.
- KGit instead of system `git`. JGit core lacks SSH, so `org.eclipse.jgit.ssh.apache` + `.agent` are added.
  JGit ignores git credential helpers: private HTTPS remotes need SSH.
- `get_standard` matches only existing file names, so path traversal is impossible.
- `application` / `installDist` instead of the shadow plugin; JaCoCo instead of Kover (both built into Gradle).
- The kotlin-logging banner is disabled in `main` before the SDK loads: it prints to stdout, which carries MCP.
  slf4j-simple logs to stderr.
- MCP client tests run on `Dispatchers.Default` inside `runTest`: on virtual time the client's request timeout
  fires before the server answers from real threads.
- `MainKt` (argument check + stdio) is the only untested code; it is verified by a stdio smoke test.
- History rewrite used `git commit-tree` so the index and working tree (staged `.idea` files) stayed untouched.

## Usage
```
./gradlew installDist
claude mcp add --scope user standards -- \
  /home/diether18/IdeaProjects/server-mcp-kotlin/build/install/server-mcp-kotlin/bin/server-mcp-kotlin \
  /home/diether18/IdeaProjects/server-mcp-kotlin/standards
```

## Next steps
- Add a git remote, push `main` and `feature-1/apply-standards`, open a PR and squash-merge it.
- Until a remote exists, every pull logs a warning and the server serves local files.
- Standards follow-ups: clarify "the app starts" vs. the Statistics-UI desktop ban (`05`, `08`), and
  "ponytail without `ponytail:` markers" in `08`.
- Staged `.idea/` files and `server-mcp-kotlin.iml` are the user's and are not committed.
