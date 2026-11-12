# Backend

Applies to every backend service. Framework specifics are in the language directory, for Kotlin
[../kotlin/04-ktor.md](../kotlin/04-ktor.md) and [../kotlin/05-persistence.md](../kotlin/05-persistence.md).

## Layout

- New projects: one package per feature or domain (`order`, `user`, `ingest`). The layer shows in the file name
  (`OrderRoutes`, `OrderService`, `OrderRepository`); data classes go into `<feature>/model`, DTOs into
  `<feature>/model/dto`, validation rules into `<feature>/validation`.
- Shared infrastructure stays outside the feature packages:

| Package | Holds |
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

- Rules live in `<feature>/validation` and run in the framework's validation step.
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

## Identity and roles

- The OIDC provider owns users, credentials and roles. Apps keep no user table, no passwords and no local
  sign-in.
- Web and mobile clients sign in with Authorization Code and PKCE as public clients; no client secret ships in a
  client. Backends accept only bearer access tokens.
- A backend verifies every token: signature through the issuer's JWKS, `iss`, `aud` (the app's client ID), `exp`
  and `nbf`. Tokens from any other issuer are rejected.
- Roles come from the `groups` claim, named `<app>-<role>`, for example `statistics-admin`. An app reads only
  groups with its own prefix and maps them to permissions in one place. Deny by default.
- No token or an invalid token gets 401. A valid token without the needed role gets 403. Both use the shared
  error body.
- Users are identified by `sub`, never by e-mail. Records store the owner's `sub`; name and e-mail come from the
  token and are copied only when a record must keep them.
- A role change takes effect with the next token. Roles are never cached beyond the token's lifetime.
- The UI hides what a role cannot do, but only the backend enforces it.
- Service-to-service calls use the client credentials grant. Devices and brokers that cannot use OIDC get
  per-client tokens: 32 random bytes, base64url, stored only as a SHA-256 hash.
- Existing apps with local users move to this model when their auth code is next changed.
