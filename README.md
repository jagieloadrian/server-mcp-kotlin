# server-mcp-kotlin

MCP server that serves my coding standards (`standards/*.md`) to AI agents, so every project follows the same
rules. Server `instructions` tell the client to read the relevant standards before writing, reviewing or
committing code.

## Tools

| Tool | Input | Output |
| --- | --- | --- |
| `list_standards` | none | `- <name>: <first heading>` per standard |
| `get_standard` | `name` from `list_standards` | full markdown, or an error listing the available names |

## Run locally (stdio)

```bash
./gradlew installDist
claude mcp add --scope user standards -- \
  "$PWD/build/install/server-mcp-kotlin/bin/server-mcp-kotlin" "$PWD/standards"
```

The only argument is the standards directory. Files are read on every call, so edits need no rebuild. At most
once every 24 h a tool call fast-forwards the git repo that holds the directory (KGit/JGit, `FF_ONLY`, no merge or
rebase). A failed or diverged pull logs a warning and the server keeps serving the local files. SSH remotes work
through the SSH agent; JGit ignores git credential helpers.

## Use the homelab instance (HTTP)

```bash
claude mcp add --transport http --scope user standards http://server-mcp-kotlin.home.arpa/mcp
```

## Configuration

| Variable | Default | Meaning |
| --- | --- | --- |
| `MCP_HTTP_HOSTS` | unset | Comma-separated `Host` values to accept. Set: HTTP on `0.0.0.0:8080`. Unset: stdio. |
| `LOG_TARGET` | `System.err` | Where the JSON logs go. stdout carries JSON-RPC in stdio mode, so keep the default there. |

HTTP endpoints:

- `POST /mcp`: stateless Streamable HTTP. Requests with any other `Host` get `403`.
- `GET /health/live`, `GET /health/ready`: `200 OK`.

Callers: Claude Code on the LAN through `home-gateway`. The HTTP instance calls nothing; the standards are in the
image.

## Deployment

The app follows the homelab platform contract and `standards/07-devops-homelab.md`.

1. A push to `main` runs `.github/workflows/build.yml`: `./gradlew check build`, a Jib multi-arch image
   (`amd64`, `arm64`) pushed to `ghcr.io/jagieloadrian/server-mcp-kotlin:main-<yyyyMMddHHmmss>-<sha7>`, and a
   commit of that tag to `.devops/helm/release.yaml`. A `v*` tag publishes a semver image without a deploy.
2. Flux reads `.devops/helm/` once a day and upgrades the HelmRelease of the shared chart `charts/app` from
   homelab-infra. To deploy at once:
   ```bash
   flux reconcile kustomization server-mcp-kotlin -n flux-system --with-source
   ```
3. `.github/workflows/devops.yml` renders the release with the chart and validates it with kubeconform.

The image contains the standards at `/standards`, so a standards change reaches the cluster with the next image.

First deploy:

1. In homelab-infra: `scripts/new-app.sh own server-mcp-kotlin`, then merge.
2. Protected `main`: set `DEPLOY_APP_CLIENT_ID` and `DEPLOY_APP_PRIVATE_KEY` as in homelab-infra `docs/apps.md`.

The GHCR package can stay private: the cluster pulls with Secret `ghcr-pull` from homelab-infra (`docs/setup.md`, step 3).

Local preview of the manifests:

```bash
yq .spec.values .devops/helm/release.yaml | helm template server-mcp-kotlin <homelab-infra>/charts/app -f -
```

## Development

```bash
./gradlew check build
./gradlew jibDockerBuild -Djib.from.platforms=linux/amd64 -Djib.to.image=server-mcp-kotlin:local
```

`check` runs ktlint (`ktlint_official`), the tests and JaCoCo verification (line above 80 %, branch above 75 %).
Kotlin warnings are errors. Versions live in `gradle/libs.versions.toml` and Renovate keeps them current.
