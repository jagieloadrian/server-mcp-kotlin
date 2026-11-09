# Kotlin coroutines

## Rules

- No `runBlocking` in production code. Ktor 3 loads `suspend fun Application.module()`; tests use `runTest`.
- No raw `Thread` / `thread {}`. Long-running loops run in a coroutine with an owned scope. Use `Dispatchers.IO`
  when the loop calls blocking clients such as `KafkaConsumer.poll`.
- Loops check `isActive`, back-off waits use `delay`, so cancelling stops them at once.
- Work that must finish once started (store, then ack) runs in `withContext(NonCancellable)`.
- `close()` cancels the job and waits for it with a bounded timeout.
- Blocking JDBC: see [05-persistence.md](05-persistence.md).

```kotlin
private val worker =
    CoroutineScope(Dispatchers.IO + CoroutineName("$name-consumer")).launch(start = CoroutineStart.LAZY) { consume() }

private suspend fun handle(message: BrokerMessage): Delivery =
    if (active()) withContext(NonCancellable) { store(message) } else Delivery.RETRY
```
