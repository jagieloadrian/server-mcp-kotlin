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

### 2026-11-04, `feature-2/homelab-deploy` (on top of `feature-1/apply-standards`, not merged)
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
- History rewrite used `git commit-tree` so the index and working tree (staged `.idea` files) stayed untouched.

## Usage
See README.md: local stdio via `installDist` + `claude mcp add`, homelab via
`claude mcp add --transport http --scope user standards http://server-mcp-kotlin.home.arpa/mcp`.

## Next steps
- Squash-merge the `feature-1/apply-standards` PR, then push `feature-2/homelab-deploy` and open its PR.
- After the first build on `main`: make the GHCR package public, push and merge homelab-infra
  `feature-8/server-mcp-kotlin`, `flux reconcile kustomization server-mcp-kotlin -n flux-system --with-source`.
- Protected `main`: set `DEPLOY_APP_CLIENT_ID` / `DEPLOY_APP_PRIVATE_KEY` (homelab-infra `docs/apps.md`).
- Not verified locally: kubeconform (binary download was blocked) and the arm64 image; CI covers the first,
  the RPi rollout the second.
- Open risk: `startupProbe` and memory measured on amd64 only; check RAM on the RPi after the first rollout.
- Standards follow-ups: clarify "the app starts" vs. the Statistics-UI desktop ban (`05`, `08`), and
  "ponytail without `ponytail:` markers" in `08`.
- Staged `.idea/` files and `server-mcp-kotlin.iml` are the user's and are not committed.
