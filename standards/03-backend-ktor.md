# Backend (Ktor)

Applies to StatisticsService and mon-home-ass.

## Layout

| Package | Holds |
|---------|-------|
| `controller` | Routes and StatusPages. No business rules, no validation calls |
| `service` | Business rules |
| `repository` | Exposed queries |
| `model`, `model/dto` | Data classes, API DTOs |
| `validation` | RequestValidation rules and shared validation helpers |
| `config` | Loading and validating configuration |
| `di` | Ktor DI wiring, monitoring, framework setup |
| `db` | Connection pool, migrations, transactions |
| `exception` | Typed API exceptions |

## Configuration

- `EngineMain` with `application.yaml`; values come from env vars (12-factor).
- Local profile: a layered `-config` file (the later file wins), for example `.devops/application-local.yaml`,
  started by a `runLocal` Gradle task. It runs without OIDC.
- `ApplicationConfig` is read in one place only (`AppConfigLoader`) into a typed `AppConfig`.
- One loader per section (db, oidc, kafka, jetstream). Each loader collects problems into a list, and startup
  fails once with every problem listed:

```kotlin
check(problems.isEmpty()) { "Invalid configuration: ${problems.joinToString("; ")}" }
```

## Request validation

- Ktor `RequestValidation`. Rules live in the `validation` package.
- Route handlers never call validators, never parse to validate, and never persist anything for invalid input.
- Normalise values in one shared function (`normalizeEmail`), not per route.

## Errors

- StatusPages maps every error to one body: `{ "error": ..., "message": ..., "details": [...] }`.
- Every 4xx has that body, including 401 and 429.
- Never echo the request body back. Malformed JSON gets a generic 400.
- Reject empty or meaningless input (an empty dataset) at validation.

## Dependency injection

- Ktor DI: `dependencies { provide { ... } }`.
- `AutoCloseable` dependencies close in reverse declaration order. Declare consumers after what they use, so a
  broker consumer stops before the database pool closes.

## Database

- Exposed (`org.jetbrains.exposed.v1.*`) and Flyway on PostgreSQL.
- Never edit a Flyway migration that has been applied. Add a new one.
- pgjdbc timeouts are passed as strings: `connectTimeout=2`, `socketTimeout=60`. Numeric values are ignored
  silently.
- Use a read-only database user when the app only reads. mon-home-ass never writes to the Home Assistant recorder
  database.
- All records go through one record store abstraction so the backend stays swappable.

## API contract

- Contracts are versioned. A breaking change needs a new version, not a silent change.
- mon-home-ass: OpenAPI comes from the routes' `.describe {}` blocks (`/openapi.json`, `/swagger`), never from a
  hand-edited file.
- StatisticsService: contracts are documented in `.docs`.

## Observability

- `/health/live` for the process, `/health/ready` for the database. Readiness does not depend on optional
  brokers.
- JSON logs to stdout (logstash encoder) with `timestamp`, `level`, `message`, `logger`, `traceId`. `traceId`
  comes from `traceparent`. `LOG_FORMAT=plain` for local runs.
- Micrometer Prometheus at `/metrics`.

## HTTP edge

- `XForwardedHeaders` only when `TRUST_PROXY=true`. Without it, every user behind a proxy shares one client-IP
  rate limit.
- CORS hosts come from config.

## Shutdown

- Drain in-flight requests, then stop Netty, then close dependencies. Total time must stay at or under 20 s.

## Messaging

- Use the official clients: `org.apache.kafka:kafka-clients` and `io.nats:jnats`.
- One shared pipeline (token check, rejections, metrics, back-off from 1 s to 30 s). Broker classes only poll
  and ack.
- Ack only after a durable store. Transient SQL errors are retried; payload errors are acked and recorded as
  rejected.
- Redelivery must be idempotent. Pub/sub sources need identity fields in every mapping rule.

## Security

- Passwords: PBKDF2-HMAC-SHA256 through JCA, 600 000 iterations, random salt, constant-time comparison, and a
  decoy hash when the user does not exist.
- Tokens: 32 random bytes, base64url, stored only as a SHA-256 hash.
- Local sign-in and OIDC both resolve to platform users. Roles stay in the platform, not in the identity
  provider.
