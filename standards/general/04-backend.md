# Backend

Applies to every backend service. 

## Layout

- New projects: one package per feature or domain (`order`, `user`, `ingest`). The layer shows in the file name
  (`OrderRoutes`, `OrderService`, `OrderRepository`); data classes go into `<feature>/model`, DTOs into
  `<feature>/model/dto`.
- Shared infrastructure stays outside the feature packages in both layouts.

| Layer | Holds |
|-------|-------|
| `config` (shared) | Loading and validating configuration |
| `di` (shared) | DI wiring, monitoring, framework setup |
| `db` (shared) | Connection pool, migrations, transactions |
| `exception` (shared) | Typed API exceptions |

## Configuration

- Values come from env vars (12-factor).
- Configuration is read in one place only into a typed config object.
- One loader per section (db, oidc, kafka, jetstream etc.). Each loader collects problems into a list, and startup
  fails once with every problem listed.

## Request validation

- Rules live in the `validation` package and run in the framework's validation step.
- Route handlers never call validators, never parse to validate, and never persist anything for invalid input.
- Normalize values in one shared function (`normalizeEmail`), not per route.

## Errors

- One error mapper maps every error to one body: `{ "error": ..., "message": ..., "details": [...] }`.
- Every 4xx has that body, including 401 and 429.
- Never echo the request body back. Malformed JSON gets a generic 400.
- Unknown routes get 404 with the same body. Any other unexpected exception gets 500 with a generic message and is
  logged with its stack trace.
- Reject empty or meaningless input (an empty dataset) at validation.

## Dependency injection

- Closeable dependencies close in reverse declaration order. Declare consumers after what they use, so a broker
  consumer stops before the database pool closes.

## Database

- Never edit a database migration that has been applied. Add a new one.
- Use a read-only database user when the app only reads. 
- All records go through one record store abstraction so the backend stays swappable.

## API contract

- Contracts are versioned. A breaking change needs a new version, not a silent change.
- OpenAPI is generated from the routes, never from a hand-edited file.

## Observability

- `/health/live` for the process, `/health/ready` for the database. Readiness does not depend on optional
  brokers.
- JSON logs to stdout with `timestamp`, `level`, `message`, `logger`, `traceId`. `traceId` comes from
  `traceparent`. Plain logs for local runs.
- Prometheus metrics at `/metrics`.
- Never log secrets, tokens, passwords or request bodies.

## HTTP edge

- Trust `X-Forwarded-*` headers only when `TRUST_PROXY=true`. Without it, every user behind a proxy shares one
  client-IP rate limit.
- CORS hosts come from config.

## Shutdown

- Drain in-flight requests, then stop the server, then close dependencies. Total time must stay at or under 20 s.

## Messaging

- Use the official clients of the broker.
- One shared pipeline (token check, rejections, metrics, back-off from 1 s to 30 s). Broker classes only poll
  and ack.
- Ack only after a durable store. Transient SQL errors are retried; payload errors are acked and recorded as
  rejected.
- Redelivery must be idempotent. Pub/sub sources need identity fields in every mapping rule.

## Security

- Passwords: PBKDF2-HMAC-SHA256, 600 000 iterations, random salt, constant-time comparison, and a decoy hash when
  the user does not exist.
- Tokens: 32 random bytes, base64url, stored only as a SHA-256 hash.
- Local sign-in and OIDC both resolve to platform users. Roles stay in the platform, not in the identity
  provider.
