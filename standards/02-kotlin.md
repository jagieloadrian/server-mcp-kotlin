# Kotlin

Base: [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).

## Formatting and build

- ktlint with `ktlint_official` through the `org.jlleitschuh.gradle.ktlint` plugin. `./gradlew check` fails on
  violations.
- `.editorconfig`:

```editorconfig
root = true

[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true

[*.{kt,kts}]
indent_style = space
indent_size = 4
ktlint_code_style = ktlint_official
ktlint_function_naming_ignore_when_annotated_with = Composable
```

- `allWarningsAsErrors = true`. Today only StatisticsService sets it; new and touched projects should add it.
- JVM toolchain 21 for JVM code. Current exceptions: DatabaseScheduleExecutor uses 17, and SmartConnectLumBunny
  targets `JVM_11` for Android.
- Versions live in `gradle/libs.versions.toml` and are kept current by Renovate or Dependabot.

## No comments

- No `//`, no `/* */`, no KDoc, no `TODO`, no `ponytail:` markers. This covers Kotlin, Gradle scripts, YAML,
  shell, Dockerfiles, workflows and code blocks in Markdown.
- Allowed: `// given`, `// when`, `// then` in tests, shebangs, `<!-- SPECKIT START -->` / `<!-- SPECKIT END -->`
  and the IntelliJ `<!-- Plugin description -->` markers.
- Names explain what code does. The reason for a choice goes into the commit body.

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

## Coroutines

- No `runBlocking` in production code. Ktor 3 loads `suspend fun Application.module()`; tests use `runTest`.
- No raw `Thread` / `thread {}`. Long-running loops run in a coroutine with an owned scope. Use `Dispatchers.IO`
  when the loop calls blocking clients such as `KafkaConsumer.poll`.
- Loops check `isActive`, back-off waits use `delay`, so cancelling stops them at once.
- Work that must finish once started (store, then ack) runs in `withContext(NonCancellable)`.
- `close()` cancels the job and waits for it with a bounded timeout.
- Blocking JDBC runs through `withContext(Dispatchers.IO)` in one place (`Db.tx`), not in every caller.

```kotlin
private val worker =
    CoroutineScope(Dispatchers.IO + CoroutineName("$name-consumer")).launch(start = CoroutineStart.LAZY) { consume() }

private suspend fun handle(message: BrokerMessage): Delivery =
    if (active()) withContext(NonCancellable) { store(message) } else Delivery.RETRY
```

## Logging

- `io.github.oshai:kotlin-logging` with lazy messages: `logger.warn { "..." }`.
- Never log secrets, tokens, passwords or request bodies.
