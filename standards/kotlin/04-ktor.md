# Ktor

Framework-neutral backend rules:
[../general/04-backend.md](../general/04-backend.md).

## Routes

- One `fun Route.orderRoutes()` extension per feature, installed from the module. Routes stay thin: read the
  request, call the service, respond.

## Configuration

- `EngineMain` with `application.yaml`; values come from env vars.
- Local profile: a layered `-config` file (the later file wins), for example `.devops/application-local.yaml`,
  started by a `runLocal` Gradle task. It replaces OIDC with a fixed local user whose groups are set in that file.
- `ApplicationConfig` is read in one place only (`AppConfigLoader`) into a typed `AppConfig`. Each section loader
  collects problems, and startup fails once:

```kotlin
check(problems.isEmpty()) { "Invalid configuration: ${problems.joinToString("; ")}" }
```

## Request validation

- Ktor `RequestValidation`. Rules live in the `validation` package.

## Errors

- StatusPages maps every error to the shared error body: `exception<Throwable>` logs and answers 500,
  `status(HttpStatusCode.NotFound)` covers unknown routes.

## WebSockets

- `install(WebSockets)` with explicit `pingPeriod`, `timeout` and `maxFrameSize`.
- Remove a session from any shared registry in `finally`. Skip frame types the endpoint does not handle.

## Dependency injection

- Ktor DI: `dependencies { provide { ... } }`.
- `AutoCloseable` dependencies close in reverse declaration order.

## API contract

- OpenAPI comes from the routes' `.describe {}` blocks (`/openapi.json`, `/swagger`).

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

- Bearer tokens through `ktor-server-auth-jwt`: `jwt("oidc")` with `verifier(jwkProvider, issuer) {
  withAudience(clientId) }`. The `JwkProvider` comes from `JwkProviderBuilder(URL(jwksUri))` with caching and rate
  limiting; `jwksUri` comes from the issuer's discovery document.
- One route-scoped plugin checks the `groups` claim. Each protected route declares the role it needs.
- Device tokens: `SecureRandom` and SHA-256 through JCA.
