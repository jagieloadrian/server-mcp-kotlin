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

### 2026-11-04, `feature-1/apply-standards` (squash-merged as #1)
- Code follows `02-kotlin.md`: no comments, `suspend fun main` instead of `runBlocking`, kotlin-logging with
  slf4j-simple, constants instead of magic strings, `internal` visibility, `CancellationException` rethrown.
- Server wiring extracted to `standardsServer(...)`; `main` only validates the argument and attaches stdio.
- Tests follow `05-testing.md`: JUnit 5, Kotest assertions, `runTest`, `should … when …`, given/when/then.
  14 tests: every sync path (pull, throttle, diverged history, unreachable remote, no git repo), listing,
  lookup, and all tools through an in-process MCP client.
- Gates in `./gradlew check build`: ktlint (`ktlint_official`), `allWarningsAsErrors`, JaCoCo line > 80 %,
  branch > 75 % (now 84 % / 79 %). Versions in `gradle/libs.versions.toml`, `renovate.json` added.

### 2026-11-04, `feature-2/homelab-deploy` (squash-merged as #2)
- HTTP mode for the homelab: `MCP_HTTP_HOSTS` set = Ktor CIO on `0.0.0.0:8080`, stateless Streamable HTTP on
  `POST /mcp`, `GET /health/live` and `/health/ready`. Unset = stdio as before.
- Logs: logback + logstash encoder, JSON lines (`timestamp`, `level`, `message`, `logger`, `traceId`), stderr by
  default, `LOG_TARGET=System.out` in the cluster.
- Jib multi-arch image (`amd64`, `arm64`, UID 10001, temurin 21 JRE) with the standards at `/standards`.
- `.devops/helm/release.yaml` for the homelab-infra chart `charts/app`, host `server-mcp-kotlin.home.arpa`.
- CI: `build.yml` (check build, Jib push to GHCR, tag bump commit), `devops.yml` (helm template + kubeconform).
- README.md. 18 tests, coverage without `MainKt`: line 97 %, branch 88 %.
- Verified: stdio smoke test (stdout only JSON-RPC, logs on stderr), HTTP smoke test (probes 200, initialize,
  `get_standard`, foreign `Host` 403), image under contract limits (read-only FS, 256 MiB, caps dropped,
  UID 10001: ready in ~2 s, 157 MiB, `docker stop` < 1 s), `helm template` with the chart.
- homelab-infra branch `feature-8/server-mcp-kotlin` (not pushed): `scripts/new-app.sh own server-mcp-kotlin`,
  architecture diagram, its PROJECT-STATUS.

### 2026-11-09, `feature-3/standards-by-language` (squash-merged as #3)
- `standards/` split into `general/` (principles, git, testing, backend, UI/UX, homelab, AI assistants) and
  `kotlin/` (language, coroutines, build, Ktor, persistence, Compose MP, testing, Spring Boot, Java to Kotlin,
  Apollo GraphQL). Mixed files were split; the restructure commit adds or drops no rule.
- The server walks subdirectories; a standard's name is its path without `.md` (`kotlin/02-coroutines`).
  2 new tests, 20 in total; coverage line 97 %, branch 90 % (without `MainKt`).
- Rules added from Kotlin/kotlin-agent-skills and lazyskills.sh Kotlin skills, one commit per topic, each
  commit body naming what was left out.
- Verified: `./gradlew check build`, stdio smoke test listing all 18 standards and reading nested ones, every
  relative link in `standards/` resolves, no comments in Markdown code blocks.

### 2026-11-11, `feature-4/manual-editing` (not merged)
- User commits drop project names from the standards and add `general/08-sdd-speckit-flow.md`.
- Review fixes in one commit: leftover project names, spec-kit order and loops, Kotlin details out of
  `general/`, `<feature>/validation`, PR titles as Conventional Commits, commit body at most 120 characters.
- New `general/09-agents.md`: delegation (search, review, git), checking agent output, context, project setup,
  building agent apps (tools, MCP, prompts, evals). Claude Code and Claude API as examples; no GSD.
- Verified: `./gradlew check build`, stdio smoke test lists `general/08` and `general/09`, links resolve.

## Decisions
- Two transports: stdio for the local `claude mcp add`, HTTP for the homelab, chosen by `MCP_HTTP_HOSTS`.
- Stateless Streamable HTTP: both tools are read-only, so no session store and any replica can answer.
- `MCP_HTTP_HOSTS` feeds the SDK's DNS rebinding protection (Host allow list). Origin is not checked: the data
  is public and read-only. No auth: LAN only, AuthorizationPolicy admits only the gateway.
- Standards baked into the image instead of a git clone in the pod: no credentials, no writable volume, a
  standards push ships with the next image. In the pod `sync()` logs "not in a git repository" once a day.
- Logback root level WARN: the MCP SDK logs every tool registration at INFO, and HTTP mode builds a server per
  request.
- `MainKt` excluded from JaCoCo: transport choice and socket/stdio binding, verified by smoke tests.
- SIGTERM exits with 143 (JVM default), not 0 as the contract text says. Shutdown takes < 1 s; same as the other
  Ktor apps.
- CI copies mon-home-ass without Codecov, OpenAPI and the secret-key guard (no secrets here).
- Files are read from disk on every call, so md edits need no rebuild.
- Pull is lazy (on a tool call, at most every 24 h). Add a background poller only if docs must refresh while idle.
- FF_ONLY + `setRebase(false)`: never merges, rebases or overwrites local edits (ignores `pull.rebase`).
  A diverged copy keeps serving local files.
- KGit instead of system `git`. JGit core lacks SSH, so `org.eclipse.jgit.ssh.apache` + `.agent` are added.
  JGit ignores git credential helpers: private HTTPS remotes need SSH.
- `get_standard` matches only existing file names, so path traversal is impossible.
- `application` / `installDist` for local use, Jib for the image; JaCoCo instead of Kover (built into Gradle).
- The kotlin-logging banner is disabled in `main` before the SDK loads: it prints to stdout, which carries MCP.
- MCP client tests run on `Dispatchers.Default` inside `runTest`: on virtual time the client's request timeout
  fires before the server answers from real threads.
- Standards: `general/` plus one directory per language, so Python or C/C++ can be added as `python/`, `cpp/`
  without touching `general/`. Numeric prefixes keep reading order in `list_standards`.
- Skill-pack conflicts, decided with the user on 2026-11-09:
  - Errors stay typed exceptions mapped at the boundary (ECC `Result` advice not taken).
  - TDD is required for new logic and bug fixes; tiered coverage targets not taken.
  - New backends, Ktor included, are packaged by feature; existing projects keep layer-first.
  - Gradle stays the default build; Kotlin Toolchain rules apply only to projects that already use it.
  - KMP iOS, AGP 9 and immutable-collections skills not taken.
- Conflicts resolved by the existing rules without asking: Koin in the backend, `newSuspendedTransaction`, H2
  tests, LoggerFactory companions, detekt, raw `./gradlew` ban, KDoc in Java conversion, separate `git mv`
  commit. Stored coroutine scopes are allowed in classes that cancel them in `close()`.
- Spec-kit features use `feature-<n>/` branches; `analyze` runs up to five rounds and stops at a clean round.
- Project-specific safety bans removed from the standards stay out; they were not moved to project files.
- History rewrite used `git commit-tree` so the index and working tree (staged `.idea` files) stayed untouched.

## Usage
See README.md: local stdio via `installDist` + `claude mcp add`, homelab via
`claude mcp add --transport http --scope user standards http://server-mcp-kotlin.home.arpa/mcp`.

## Next steps
- Push `feature-4/manual-editing`, open its PR with a Conventional Commit title and squash-merge it.
- Open risk: spec-kit scripts may expect `<NNN>-<name>` branches; check on the first spec-kit feature.
- Open risk: the Redis archive (StatisticsService) and DFU/OTA (SmartConnectLumBunny) bans no longer live in
  any file those repos load. The desktop-app ban stays, generalized, in `kotlin/06`.
- Homelab: make the GHCR package public after the first build on `main`, push and merge homelab-infra
  `feature-8/server-mcp-kotlin`, `flux reconcile kustomization server-mcp-kotlin -n flux-system --with-source`.
- Protected `main`: set `DEPLOY_APP_CLIENT_ID` / `DEPLOY_APP_PRIVATE_KEY` (homelab-infra `docs/apps.md`).
- Open risk: RAM and startup measured on amd64 only; check on the RPi after the first rollout.
- Open risk: the daily git pull runs inside the first tool call and took about 20 s over SSH locally. JGit also
  warns "EdDSA provider not supported" for an ed25519 key from the SSH agent.
- Standards follow-ups: "ponytail without `ponytail:` markers" in `general/07`. Python and C/C++ directories when
  needed.
