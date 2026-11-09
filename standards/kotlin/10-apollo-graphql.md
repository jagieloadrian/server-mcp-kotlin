# Apollo Kotlin (GraphQL)

Applies to GraphQL clients in Kotlin (Android, JVM, KMP).

## Setup

- Apollo Kotlin 5 or newer, never 3 or older.
- Gradle plugin `com.apollographql.apollo`; `apollo-runtime` without its own version, the plugin aligns it.
- One `service("...")` per endpoint with an explicit `packageName`.
- The schema is checked in: `src/main/graphql/schema.graphqls`, KMP `src/commonMain/graphql/`. Refresh it with
  `downloadServiceApolloSchemaFromIntrospection`.
- Custom scalars through `mapScalar(...)`; `apollo-kotlin-adapters` for `BigDecimal`, `Instant` and similar.
- Multi-module: exactly one schema module. It and every module that shares fragments set
  `generateApolloMetadata.set(true)`; feature modules use `dependsOn(project(":schema"))`, the schema module
  declares `isADependencyOf(project(":feature"))`.

## Client

- One `ApolloClient` per service, provided through DI.
- Auth headers in one interceptor. `LoggingInterceptor` only in debug builds.

## Operations

- One operation per `.graphql` file, named with its type suffix: `GetUserQuery`, `UpdateUserMutation`,
  `OnMessageReceivedSubscription`. Shared fragments in their own files.
- Select only the fields the code uses.
- After `execute()`, check `response.exception` (network, HTTP, parsing) and `response.errors` (GraphQL errors;
  `data` may arrive with them). `dataOrThrow()` only where any error is fatal.
- Subscriptions use `toFlow()`, never `execute()`.

## Normalized cache

- Only when needed: offline use, fast start, objects shared between screens, or mutations that must update every
  view.
- The new artifacts `com.apollographql.cache:normalized-cache` (memory) and `normalized-cache-sqlite`, not the
  classic `com.apollographql.apollo` cache.
- Cache keys in `extra.graphqls`: `@typePolicy(keyFields: "id")` for every cached type, `@fieldPolicy` for fields
  that return an object by argument.
- The cache is the UI's single source of truth, observed with `.watch()`. Default policy `CacheFirst`; others per
  query with `.fetchPolicy(...)`.
