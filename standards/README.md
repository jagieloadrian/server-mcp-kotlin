# Coding Standards

Shared rules for my projects. They come from how the projects are built today and from corrections made
while working on them.

## Layout

- `general/`: rules for every project and language.
- `<language>/`: how the general rules apply in one language, plus its own idioms, tools and frameworks. Today
  only `kotlin/`. A new language gets its own directory with the same kind of files.

The MCP server names each standard by its path without `.md`, for example `kotlin/02-coroutines`.

### general

| File | Covers |
|------|--------|
| [01-principles.md](general/01-principles.md) | YAGNI, KISS, DRY, SOLID, minimal change, no comments, language of code and docs |
| [02-git-workflow.md](general/02-git-workflow.md) | Branches, commits, verification, pushing, PROJECT-STATUS.md, spec-kit flow |
| [03-testing.md](general/03-testing.md) | What to test, test first (TDD), naming, given/when/then, mocking, real dependencies, coverage, checklist |
| [04-backend.md](general/04-backend.md) | Package by feature, config, validation, errors, database, observability, messaging, security |
| [05-ui-ux.md](general/05-ui-ux.md) | Typography, colour and contrast, spacing, states, forms, content, accessibility, release checklist |
| [06-devops-homelab.md](general/06-devops-homelab.md) | Images, Helm/Flux deploy, runtime contract, secrets, local compose |
| [07-ai-assistants.md](general/07-ai-assistants.md) | Rules for Claude and other agents working in these repos |

### kotlin

| File | Covers |
|------|--------|
| [01-language.md](kotlin/01-language.md) | Null safety, Java interop, declarations, value classes, idioms, errors, logging |
| [02-coroutines.md](kotlin/02-coroutines.md) | Ownership and scopes, blocking calls, cancellation, Flow primitives |
| [03-build.md](kotlin/03-build.md) | Gradle, ktlint, `.editorconfig`, warnings as errors, JVM toolchain, versions; Kotlin Toolchain projects |
| [04-ktor.md](kotlin/04-ktor.md) | Ktor routes, config, validation, StatusPages, DI, WebSockets, OpenAPI, logging, shutdown |
| [05-persistence.md](kotlin/05-persistence.md) | Exposed (DSL, migrations, batching, `LIKE` escaping), Flyway, pgjdbc, JPA and Hibernate entities |
| [06-compose-multiplatform.md](kotlin/06-compose-multiplatform.md) | Compose Multiplatform clients: shared code, UI layer, Koin, platform code |
| [07-testing.md](kotlin/07-testing.md) | Kotest, JUnit 5, MockK, assertions, `testApplication`, coroutine tests, Koin, coverage tools |
| [08-spring-boot.md](kotlin/08-spring-boot.md) | Spring Boot: build plugins, injection, config, Bean Validation, transactions, test slices |
| [09-java-to-kotlin.md](kotlin/09-java-to-kotlin.md) | Converting Java files: four passes, checklist, interop and framework pitfalls |
| [10-apollo-graphql.md](kotlin/10-apollo-graphql.md) | Apollo Kotlin: setup, client, operations, errors, normalized cache |

Testing and UI come from `~/Documents/AI/specs/TEST_SPECIFICATION.md` and `~/Documents/AI/Czytelny Interfejs.md`.

## Projects

All projects follow `general/01`, `02`, `03` and `07`, and `kotlin/01`–`03` and `07`.

| Project | Stack | Also |
|---------|-------|------|
| StatisticsService | Ktor 3, Exposed, Flyway, PostgreSQL, Kafka / NATS JetStream | general/04, 06; kotlin/04, 05 |
| Statistics-UI | Compose Multiplatform (wasmJs, js, JVM desktop), Ktor client, Koin | general/05, 06; kotlin/06 |
| mon-home-ass | Ktor 3, Exposed, read-only PostgreSQL (Home Assistant recorder), OpenAPI | general/04, 06; kotlin/04, 05 |
| SmartConnectLumBunny | KMP (Android + desktop JVM), Compose, Koin, BLE | general/05; kotlin/06 |
| DatabaseScheduleExecutor | Kotlin JVM CLI, JDBC (6 vendors), cron-utils, shadow jar | none |
| darksouls-progress-bar | IntelliJ Platform plugin, Kotlin + Java | general/05; kotlin/09 |
| server-mcp-kotlin | Ktor CIO, MCP Kotlin SDK, KGit | general/06 |

## Precedence

1. An explicit request in the current task.
2. The project's `.specify/memory/constitution.md` and `CLAUDE.md`.
3. These files.
4. The language's official conventions, for Kotlin the
   [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).

## Known divergences

- SmartConnectLumBunny: its constitution still allows one-line code comments (at most 120 characters), and the
  code has `!!` uses. Either amend the constitution to match [general/01-principles.md](general/01-principles.md)
  and [kotlin/01-language.md](kotlin/01-language.md) or keep it as a documented exception.
- `allWarningsAsErrors` is set only in StatisticsService.
- Test names: [general/03-testing.md](general/03-testing.md) asks for `should <action> when <condition>`.
  Existing tests in StatisticsService, Statistics-UI and DatabaseScheduleExecutor use free sentences, and
  darksouls-progress-bar uses both styles. New tests follow the rule; old ones are renamed only when touched.
- The homelab contract (`~/Development/homelab/Homelab Platform Contract — wymagania dla aplikacji.md`) still
  describes SOPS secrets. The apps already use Vault through ExternalSecret, see
  [general/06-devops-homelab.md](general/06-devops-homelab.md).
