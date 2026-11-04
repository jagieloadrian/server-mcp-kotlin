# Testing

Sources: `~/Documents/AI/specs/TEST_SPECIFICATION.md` and current project practice. When the two conflict, the rules
in [02-kotlin.md](02-kotlin.md) win (no comments, no `!!`).

## What to test

- Every new route, repository and piece of non-trivial logic (a branch, a loop, a parser, a security or data
  path) ships with a test.
- Test core flows and contract shapes, not every line. No tests for trivial getters or one-liners.
- A bug fix comes with the test that fails without it.
- Test at the layer the code lives in: protocol framing and crypto get parity tests against known vectors, domain
  logic gets unit tests, flows that cross the database or a broker get integration tests.

Scenarios to cover for each unit:

1. Happy path: valid input.
2. Edge cases: `null`, empty, minimum and maximum values.
3. Error cases: invalid data, exceptions.
4. Business rules and validation.
5. Integration points: calls to dependencies.
6. Boundary conditions.

## Frameworks

| Project | Framework |
|---------|-----------|
| StatisticsService, Statistics-UI, mon-home-ass | Kotest 6 `FunSpec` |
| DatabaseScheduleExecutor, darksouls-progress-bar | JUnit 5 |
| SmartConnectLumBunny | JUnit 4 (Android host and device tests), Roborazzi, Konsist |

Everywhere: Kotest assertions and MockK. Stay with the framework the project already uses. Use only libraries
that are already in `build.gradle.kts` / `libs.versions.toml`.

## Naming

- Tests: `should <action> when <condition>`.
  - JUnit: ``fun `should throw ValidationException when input is invalid`()``
  - Kotest: `test("should save entity when all validations pass")`
- One test class per production class: `UserService` → `UserServiceTest.kt`.
- Variables: `input`, `result`, `expected`, `actual`, `request`, `response`.
- Test constants: `VALID_EMAIL`, `INVALID_INPUT`, `EXPECTED_RESULT`.

## Structure

- Every test uses `// given`, `// when`, `// then`. When the call is itself the assertion (`shouldThrow`), use
  `// when & then`.
- The markers stand alone. No description after them; the code after the marker says what happens.
- No section-banner comments such as `// ===== CREATE =====`. Group with Kotest `context("create")` or JUnit
  `@Nested inner class Create`.

```kotlin
class OrderServiceTest :
    FunSpec({
        val paymentService = mockk<PaymentService>()
        val orderRepository = mockk<OrderRepository>()
        val orderService = OrderService(orderRepository, paymentService)

        context("create") {
            test("should create order when payment succeeds") {
                // given
                val request = CreateOrderRequest(items = listOf("item1"), total = 100.0)
                every { paymentService.processPayment(100.0) } returns PaymentResult.SUCCESS
                every { orderRepository.save(any()) } returns Order(id = "order123", total = 100.0)

                // when
                val result = orderService.createOrder(request)

                // then
                result.id shouldBe "order123"
                verify(exactly = 1) { paymentService.processPayment(100.0) }
            }

            test("should throw PaymentDeclinedException when payment fails") {
                // given
                every { paymentService.processPayment(any()) } returns PaymentResult.DECLINED

                // when & then
                shouldThrow<PaymentDeclinedException> {
                    orderService.createOrder(CreateOrderRequest(items = listOf("item1"), total = 100.0))
                }
            }
        }
    })
```

## Mocking

- JUnit 5: MockK annotations with the extension:

```kotlin
@ExtendWith(MockKExtension::class)
class UserServiceTest {
    @MockK
    private lateinit var userRepository: UserRepository

    @RelaxedMockK
    private lateinit var emailService: EmailService

    @InjectMockKs
    private lateinit var userService: UserService

    @Test
    fun `should create user when email is unique`() {
        // given
        val request = CreateUserRequest("john@example.com", "John Doe")
        every { userRepository.existsByEmail("john@example.com") } returns false
        every { userRepository.save(any()) } returns User(1L, "john@example.com", "John Doe")

        // when
        val result = userService.createUser(request)

        // then
        result.id shouldBe 1L
        verify { emailService.sendWelcomeEmail("john@example.com") }
    }
}
```

- Kotest: create mocks by hand with `mockk<T>()` and constructor injection.
- In unit tests, mock external systems (database, HTTP APIs, file system) and collaborators. Integration tests use
  the real dependency (see below).
- Use relaxed mocks for dependencies whose behaviour does not matter to the test. `just Runs` for `Unit` stubs.
- Verify only the calls that matter, with `verify(exactly = n)` when the count is part of the behaviour.

## Assertions

Kotest assertions only:

```kotlin
result shouldBe expected
result.shouldNotBeNull()
list shouldHaveSize 3
list shouldContain "element"
user.age shouldBeGreaterThan 18
shouldThrow<ValidationException> { validator.validate(input) }
```

## HTTP routes

- Ktor routes are tested through `testApplication` with the real module and a Testcontainers database. Check the
  status code and the response body, including the `ApiError` body on errors.

## Real dependencies

- Testcontainers for PostgreSQL, Kafka, NATS and other database vendors. One shared container setup per project
  (`testutils`).
- H2 only where the project already relies on it (mon-home-ass).
- Mock only what you do not own or cannot start.

## Coroutines

- `runTest` with virtual time. No `Thread.sleep`.
- A coroutine in `backgroundScope` that waits on `tryEmit` is not resumed by `advanceUntilIdle()`. Drive it with
  `advanceTimeBy(n)` and `runCurrent()`, and check `testScheduler.currentTime` before blaming the code.
- Waits on thread-bound code use a `CountDownLatch` with a timeout.

## Koin in tests

- Compose `KoinApplication` starts the global Koin. Call `stopKoin()` after each test that uses it.
- Graph checks use an isolated `koinApplication { modules(...) }.koin` and close it.

## Principles

- One concept per test.
- Tests are independent of each other and deterministic.
- Unit tests are fast, under 100 ms each.
- Repeated setup goes into helper functions, test data builders and shared fixtures, not into copies.
- Many similar cases: Kotest `withData` or JUnit `@ParameterizedTest`.
- Property-based tests (Kotest `checkAll`) for parsers and converters.
- Mutation testing only where it is set up.

## Coverage

- Minimum: line above 80 %, branch above 75 %, mutation above 70 % where mutation testing is used.
- Project gates can be stricter. StatisticsService runs JaCoCo verification at 88 % instruction, line and branch
  inside `check`.
- Statistics-UI and darksouls-progress-bar use Kover. Reports go to Codecov where it is configured.

## Running

```bash
./gradlew test
./gradlew test --tests "*UserServiceTest"
./gradlew check build
```

- `./gradlew check build` is green before every commit: ktlint, tests and coverage.
- Statistics-UI also compiles the js and wasm targets.
- After every implementation the app must build and start.

## Checklist for a new test

- [ ] Name follows `should <action> when <condition>`.
- [ ] Body has `// given`, `// when`, `// then` markers and no other comments.
- [ ] Dependencies are mocked (unit) or real through Testcontainers (integration).
- [ ] Assertions check the expected result, not only that nothing was thrown.
- [ ] Error scenarios are tested with `shouldThrow`.
- [ ] Important mock calls are verified.
- [ ] Variable names are clear (`input`, `result`, `expected`).
- [ ] The test compiles without warnings and passes in `./gradlew check`.

## References

- [Kotest](https://kotest.io/)
- [MockK](https://mockk.io/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
