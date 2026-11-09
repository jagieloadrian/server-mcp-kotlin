# Kotlin coroutines

## Ownership

- Every coroutine has an owner that controls its cancellation, completion and failure. If no owner is visible, do
  not add a `launch`.
- Repositories, services, use cases and data sources expose `suspend` functions and hold no `CoroutineScope`. A
  suspend API that already gives the caller cancellation, result and failure is not wrapped in a scope, `launch`,
  callback or `Deferred`.
- A stored scope is allowed in two places only:
  - a class that owns a lifecycle and cancels the scope in `close()`, such as a broker consumer;
  - a UI state holder whose scope is cancelled with the UI (`viewModelScope`, a remembered Compose scope), launched
    from a real UI event or lifecycle hook. Its lower layers stay suspending.
- Constructors and `init` blocks never launch. Work starts from a suspending bootstrap or a named start function.
- No `GlobalScope`.
- `coroutineScope { async { } }` for parallel work that fails as a whole, `supervisorScope` when children fail
  independently.

```kotlin
private val worker =
    CoroutineScope(Dispatchers.IO + CoroutineName("$name-consumer")).launch(start = CoroutineStart.LAZY) { consume() }

private suspend fun handle(message: BrokerMessage): Delivery =
    if (active()) withContext(NonCancellable) { store(message) } else Delivery.RETRY
```

## Blocking

- No `runBlocking` in production code. Ktor 3 loads `suspend fun Application.module()`, a CLI uses
  `suspend fun main`, tests use `runTest`.
- No raw `Thread` / `thread {}` and no detached `Executor.submit`. Long-running loops run in a coroutine with an
  owned scope. Use `Dispatchers.IO` when the loop calls blocking clients such as `KafkaConsumer.poll`.
- A dispatcher does not make blocking work cancellable. Wrap one interruptible JVM call in
  `runInterruptible(Dispatchers.IO)`.
- Blocking JDBC: see [05-persistence.md](05-persistence.md).

## Cancellation

- Loops check `isActive` or call `ensureActive()`, back-off waits use `delay`, so cancelling stops them at once.
- Work that must finish once started (store, then ack) runs in `withContext(NonCancellable)`, and so does
  suspending cleanup in `finally`.
- `close()` cancels the job and waits for it with a bounded timeout.
- Broad catches rethrow `CancellationException` ([01-language.md](01-language.md), Errors). `runCatching` catches it
  too: follow it with `currentCoroutineContext().ensureActive()` or end with `getOrThrow()`. Cancellation never
  becomes success.

## Flow

Pick the primitive from the consumer contract:

| Need | Primitive |
|------|-----------|
| Current state, read through `.value` | `StateFlow` |
| Hot broadcast to every collector, no `.value` | `SharedFlow` with explicit replay and buffer |
| Exactly-once event for one consumer, kept while no collector is active | `Channel(Channel.BUFFERED)` exposed as `receiveAsFlow()` |
| Independent stream per collector | cold `Flow` |

- Before choosing, state the number of consumers, what happens with no active collector, replay, buffer and
  whether `.value` is needed. If any is unknown, ask instead of picking.
- `StateFlow` needs an initial value. Model absence, loading and errors explicitly (nullable, sealed UI state),
  never with a fake placeholder.
- `update { }` for concurrent state changes. The lambda may run again, so keep it pure and fast: do I/O and read
  time or random IDs before it.
- `stateIn` is one shared property, not a function that starts a new sharing coroutine per call. Use
  `SharingStarted.Eagerly` when `.value` must be current without collectors, `WhileSubscribed` only when a stale
  value is acceptable.
- `map` on a `StateFlow` returns a plain `Flow`. End with `stateIn` when callers need `.value`.
- `catch { }` handles upstream errors. `debounce`, `distinctUntilChanged` and `mapLatest` shape UI input streams.

## Refactoring towards suspend

- Start at the class farthest from the UI. Make one public function `suspend`, follow the compiler errors, then
  delete the scope parameter that is no longer used.
