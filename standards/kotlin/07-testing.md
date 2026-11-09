# Kotlin testing

General rules (what to test, naming, structure, coverage, checklist):
[../general/03-testing.md](../general/03-testing.md).

## Frameworks

| Project | Framework |
|---------|-----------|
| StatisticsService, Statistics-UI, mon-home-ass | Kotest 6 `FunSpec` |
| DatabaseScheduleExecutor, darksouls-progress-bar | JUnit 5 |
| SmartConnectLumBunny | JUnit 4 (Android host and device tests), Roborazzi, Konsist |

Everywhere: Kotest assertions and MockK. Stay with the framework the project already uses. Use only libraries
that are already in `build.gradle.kts` / `libs.versions.toml`.

## Naming

- JUnit: ``fun `should throw ValidationException when input is invalid`()``
- Kotest: `test("should save entity when all validations pass")`
- Test file: `UserService` → `UserServiceTest.kt`.

## Structure

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
- `just Runs` for `Unit` stubs. `verify(exactly = n)` when the count is part of the behaviour.
- `coEvery` / `coVerify` for suspend functions. `slot<T>()` with `capture()` to inspect an argument.
- Never `mockk` a data class. Build a real instance.

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

## Coroutines

- `runTest` with virtual time. No `Thread.sleep`; advance with `advanceTimeBy` / `advanceUntilIdle`.
- Flows: collect a bounded part (`take(n).toList()`), or collect in `backgroundScope` and assert on the values.
- A coroutine in `backgroundScope` that waits on `tryEmit` is not resumed by `advanceUntilIdle()`. Drive it with
  `advanceTimeBy(n)` and `runCurrent()`, and check `testScheduler.currentTime` before blaming the code.
- Waits on thread-bound code use a `CountDownLatch` with a timeout.

## Koin in tests

- Compose `KoinApplication` starts the global Koin. Call `stopKoin()` after each test that uses it.
- Graph checks use an isolated `koinApplication { modules(...) }.koin` and close it.

## Parameterized and property tests

- Many similar cases: Kotest `withData` or JUnit `@ParameterizedTest`.
- Property-based tests: Kotest `checkAll` with `Arb` generators.
- One Kotest spec style per project (`FunSpec`).

## Coverage tools

- JaCoCo verification inside `check`, or Kover where the project uses it (Statistics-UI, darksouls-progress-bar).
  Reports go to Codecov where it is configured.

## Running

```bash
./gradlew test
./gradlew test --tests "*UserServiceTest"
./gradlew check build
```

- `./gradlew check build` is green before every commit: ktlint, tests and coverage.
- Statistics-UI also compiles the js and wasm targets.

## References

- [Kotest](https://kotest.io/)
- [MockK](https://mockk.io/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
