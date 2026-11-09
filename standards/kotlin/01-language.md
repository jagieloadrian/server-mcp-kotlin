# Kotlin language

Base: [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).

## Null safety

- No `!!`, in production code or in tests.
- Use `?.let`, `?:`, early `return`, `filterNotNull()`, or `checkNotNull` / `requireNotNull` with a message:

```kotlin
val (r, g, b) = checkNotNull(lchToSrgb(l, chroma, hue)) { "Chroma $chroma is outside sRGB at tone $tone" }
```

```kotlin
private suspend fun SnackbarHostState.awaitSnackbar(): SnackbarData {
    while (true) {
        currentSnackbarData?.let { return it }
        yield()
    }
}
```

## Files and packages

- The package matches the directory.
- A file with one class is named after it. Do not put six config classes into `AppConfig.kt`.
- Closely related top-level functions may share a file named after what they hold (`Tokens.kt` for `newToken`,
  `sha256`, `base64Url`).
- Data classes go into `model/` (DTOs into `model/dto/`), not into config, repository or service files.
- Validation rules go into a `validation` package.

## Declarations

- `private` by default, `internal` for helpers shared inside the module, `public` only for the real API.
- Top-level `private const val` / `private val` in `SCREAMING_SNAKE_CASE` for fixed values. No magic strings for
  roles, kinds or channel types; use constants.
- `val` over `var`, read-only collections, `buildList` / `buildMap` for assembled collections.
- Expression bodies for single-expression functions.
- Sealed interfaces for closed sets (`BrokerConfig` → `KafkaConfig`, `JetStreamConfig`) and exhaustive `when`
  without `else`.

## Errors

- Domain errors are typed exceptions with a status (`ApiException`), mapped once at the boundary.
- Never swallow an exception silently. Log it with context or turn it into a result.
- Anything that catches `Exception` inside a coroutine rethrows `CancellationException` first:

```kotlin
try {
    records.purgeRetention(definitions.datasets())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    logger.warn(e) { "Retention purge failed" }
}
```

## Logging

- `io.github.oshai:kotlin-logging` with lazy messages: `logger.warn { "..." }`.
