# DevOps and Homelab

## Images

- JVM services: Jib, base `eclipse-temurin:21-jre` or another slim JRE image, multi-arch `linux/amd64` +
  `linux/arm64`, UID 10001.
- Web clients: `nginxinc/nginx-unprivileged`, UID 10001, COPY-only Dockerfile.
- Registry: GHCR or another OCI registry. Tags are `main-<yyyyMMddHHmmss>-<sha7>` or semver. Never `latest`.
- Nothing sensitive in the image.

## Deployment

- `.devops/helm/release.yaml`: a HelmRelease of the shared infra chart `charts/app`.
- Non-secret config is inline in `values.env`.
- Secrets live in a secret manager (e.g. Vault) at `secret/apps/<app>` and arrive through ExternalSecret
  (`secrets: true`). No secret, encrypted or not, in the app repo.
- CI bumps the image tag in `release.yaml`. `devops.yml` validates with `helm template` and kubeconform.
- The first HelmRelease install retries until the secret exists.

## Runtime contract

- The cluster is the one the infra repo describes, e.g. k3s on two nodes, Istio ambient, Gateway API
  `home-gateway` in `istio-ingress`.
- Namespace `apps`, host `<app>.home.arpa`. Access is limited by AuthorizationPolicy.
- Probes on `/health/live` and `/health/ready`.
- Memory request equals limit. No CPU limit.
- Graceful shutdown in 20 s or less.
- JSON logs to stdout. Config from env vars.

## Local development

- One compose file per dependency: `.devops/<name>-compose/compose.yaml` (postgres, kafka, nats).
- A local profile without OIDC, started with one command (Kotlin: Gradle task `runLocal` with
  `.devops/application-local.yaml`).

## Dependencies

- Renovate (`renovate.json`) or Dependabot. All versions in one file (Kotlin: `gradle/libs.versions.toml`).

## Credentials

- Least privilege. Do not add a higher-privilege credential just to automate a rare manual step. Name the risk
  if a leak happens and prefer the manual step.
- Read-only database users for read-only apps.
