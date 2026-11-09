# DevOps and Homelab

Source: `~/Development/homelab/Homelab Platform Contract — wymagania dla aplikacji.md`. Applies to
StatisticsService, Statistics-UI and mon-home-ass.

## Images

- JVM services: Jib, base `eclipse-temurin:21-jre`, multi-arch `linux/amd64` + `linux/arm64`, UID 10001.
- Web clients: `nginxinc/nginx-unprivileged`, UID 10001, COPY-only Dockerfile.
- Registry: GHCR. Tags are `main-<yyyyMMddHHmmss>-<sha7>` or semver. Never `latest`.
- Nothing sensitive in the image.

## Deployment

- `.devops/helm/release.yaml`: a Flux HelmRelease of the shared homelab-infra chart `charts/app`.
- Non-secret config is inline in `values.env`.
- Secrets live in Vault at `secret/apps/<app>` and arrive through ExternalSecret (`secrets: true`). No secret,
  encrypted or not, in the app repo.
- CI bumps the image tag in `release.yaml` through a GitHub App. `devops.yml` validates with `helm template` and
  kubeconform.
- The first HelmRelease install retries until the Vault secret exists.

## Runtime contract

- k3s on two nodes (RPi 5 arm64, Wyse amd64), Istio ambient, Gateway API `home-gateway` in `istio-ingress`.
- Namespace `apps`, host `<app>.home.arpa`. Access is limited by AuthorizationPolicy.
- Probes on `/health/live` and `/health/ready`.
- Memory request equals limit. No CPU limit.
- Graceful shutdown in 20 s or less.
- JSON logs to stdout. Config from env vars.

## Local development

- One compose file per dependency: `.devops/<name>-compose/compose.yaml` (postgres, kafka, nats).
- A local profile without OIDC: StatisticsService `runLocal` with `.devops/application-local.yaml`.
- Leave legacy data archives alone. StatisticsService: the `my-redis` / `redisinsight` containers and
  `.devops/redis-compose` stay untouched as a read-only archive.

## Dependencies

- Renovate (`renovate.json`) or Dependabot. All versions in `gradle/libs.versions.toml`.

## Credentials

- Least privilege. Do not add a higher-privilege credential just to automate a rare manual step. Name the risk
  if a leak happens and prefer the manual step.
- Read-only database users for read-only apps.
