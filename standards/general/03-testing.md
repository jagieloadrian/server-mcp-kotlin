# Testing

Sources: `~/Documents/AI/specs/TEST_SPECIFICATION.md` and current project practice. When the two conflict, the rules
in [01-principles.md](01-principles.md) and the language files win (no comments, no `!!`). Frameworks, mocking
libraries and commands are in the language directory, for Kotlin [../kotlin/07-testing.md](../kotlin/07-testing.md).

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

## Naming

- Tests: `should <action> when <condition>`.
- One test class per production class: `UserService` → `UserServiceTest`.
- Variables: `input`, `result`, `expected`, `actual`, `request`, `response`.
- Test constants: `VALID_EMAIL`, `INVALID_INPUT`, `EXPECTED_RESULT`.

## Structure

- Every test uses `// given`, `// when`, `// then`. When the call is itself the assertion (`shouldThrow`), use
  `// when & then`.
- The markers stand alone. No description after them; the code after the marker says what happens.
- No section-banner comments such as `// ===== CREATE =====`. Group with the framework's nesting (Kotest
  `context("create")`, JUnit `@Nested inner class Create`).

## Mocking

- In unit tests, mock external systems (database, HTTP APIs, file system) and collaborators. Integration tests use
  the real dependency (see below).
- Use relaxed mocks for dependencies whose behaviour does not matter to the test.
- Verify only the calls that matter, with an exact count when the count is part of the behaviour.

## Real dependencies

- Testcontainers for PostgreSQL, Kafka, NATS and other database vendors. One shared container setup per project
  (`testutils`).
- H2 only where the project already relies on it (mon-home-ass).
- Mock only what you do not own or cannot start.

## Principles

- One concept per test.
- Tests are independent of each other and deterministic.
- Unit tests are fast, under 100 ms each.
- Repeated setup goes into helper functions, test data builders and shared fixtures, not into copies.
- Many similar cases: parameterized tests.
- Property-based tests for parsers and converters.
- Mutation testing only where it is set up.

## Coverage

- Minimum: line above 80 %, branch above 75 %, mutation above 70 % where mutation testing is used.
- Project gates can be stricter. StatisticsService runs JaCoCo verification at 88 % instruction, line and branch
  inside `check`.

## Running

- The project gate (Kotlin: `./gradlew check build`) is green before every commit: lint, tests and coverage.
- After every implementation the app must build and start.

## Checklist for a new test

- [ ] Name follows `should <action> when <condition>`.
- [ ] Body has `// given`, `// when`, `// then` markers and no other comments.
- [ ] Dependencies are mocked (unit) or real through Testcontainers (integration).
- [ ] Assertions check the expected result, not only that nothing was thrown.
- [ ] Error scenarios are tested.
- [ ] Important mock calls are verified.
- [ ] Variable names are clear (`input`, `result`, `expected`).
- [ ] The test compiles without warnings and passes in the project gate.
