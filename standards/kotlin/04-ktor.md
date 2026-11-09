# Ktor

Applies to StatisticsService and mon-home-ass. Framework-neutral backend rules:
[../general/04-backend.md](../general/04-backend.md).

## Configuration

- `EngineMain` with `application.yaml`; values come from env vars.
- Local profile: a layered `-config` file (the later file wins), for example `.devops/application-local.yaml`,
  started by a `runLocal` Gradle task. It runs without OIDC.
- `ApplicationConfig` is read in one place only (`AppConfigLoader`) into a typed `AppConfig`. Each section loader
  collects problems, and startup fails once:

```kotlin
check(problems.isEmpty()) { "Invalid configuration: ${problems.joinToString("; ")}" }
```

## Request validation

- Ktor `RequestValidation`. Rules live in the `validation` package.

## Errors

- StatusPages maps every error to the shared error body.

## Dependency injection

- Ktor DI: `dependencies { provide { ... } }`.
- `AutoCloseable` dependencies close in reverse declaration order.

## API contract

- mon-home-ass: OpenAPI comes from the routes' `.describe {}` blocks (`/openapi.json`, `/swagger`).

## Observability

- JSON logs through the logstash encoder. `LOG_FORMAT=plain` for local runs.
- Micrometer Prometheus at `/metrics`.

## HTTP edge

- `XForwardedHeaders` only when `TRUST_PROXY=true`.

## Shutdown

- Drain in-flight requests, then stop Netty, then close dependencies, in 20 s or less.

## Messaging

- Use the official clients: `org.apache.kafka:kafka-clients` and `io.nats:jnats`.

## Security

- PBKDF2-HMAC-SHA256 through JCA.
