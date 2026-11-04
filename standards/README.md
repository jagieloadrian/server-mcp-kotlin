# Coding Standards

Shared rules for my Kotlin projects. They come from how the projects are built today and from corrections made
while working on them.

## Files

| File | Covers |
|------|--------|
| [01-principles.md](01-principles.md) | YAGNI, KISS, DRY, SOLID, minimal change, language of code and docs |
| [02-kotlin.md](02-kotlin.md) | Kotlin style, ktlint, no comments, null safety, files, visibility, coroutines |
| [03-backend-ktor.md](03-backend-ktor.md) | Ktor services: config, validation, errors, DI, database, observability, messaging |
| [04-compose-multiplatform.md](04-compose-multiplatform.md) | Compose Multiplatform clients: shared code, UI layer, Koin, platform code |
| [05-testing.md](05-testing.md) | What to test, naming, given/when/then, mocking, Testcontainers, coverage, checklist |
| [06-git-workflow.md](06-git-workflow.md) | Branches, commits, verification, pushing, PROJECT-STATUS.md, spec-kit flow |
| [07-devops-homelab.md](07-devops-homelab.md) | Images, Helm/Flux deploy, runtime contract, secrets, local compose |
| [08-ai-assistants.md](08-ai-assistants.md) | Rules for Claude and other agents working in these repos |
| [09-ui-ux.md](09-ui-ux.md) | Typography, colour and contrast, spacing, states, forms, content, accessibility, release checklist |

Testing and UI come from `~/Documents/AI/specs/TEST_SPECIFICATION.md` and `~/Documents/AI/Czytelny Interfejs.md`.

## Projects

| Project | Stack | Files |
|---------|-------|-------|
| StatisticsService | Ktor 3, Exposed, Flyway, PostgreSQL, Kafka / NATS JetStream | 01–08 |
| Statistics-UI | Compose Multiplatform (wasmJs, js, JVM desktop), Ktor client, Koin | 01, 02, 04–09 |
| mon-home-ass | Ktor 3, Exposed, read-only PostgreSQL (Home Assistant recorder), OpenAPI | 01–03, 05–08 |
| SmartConnectLumBunny | KMP (Android + desktop JVM), Compose, Koin, BLE | 01, 02, 04–06, 08, 09 |
| DatabaseScheduleExecutor | Kotlin JVM CLI, JDBC (6 vendors), cron-utils, shadow jar | 01, 02, 05, 06, 08 |
| darksouls-progress-bar | IntelliJ Platform plugin, Kotlin + Java | 01, 02, 05, 06, 08, 09 |

## Precedence

1. An explicit request in the current task.
2. The project's `.specify/memory/constitution.md` and `CLAUDE.md`.
3. These files.
4. [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).

## Known divergences

- SmartConnectLumBunny: its constitution still allows one-line code comments (at most 120 characters), and the
  code has `!!` uses. Either amend the constitution to match [02-kotlin.md](02-kotlin.md) or keep it as a
  documented exception.
- `allWarningsAsErrors` is set only in StatisticsService.
- Test names: [05-testing.md](05-testing.md) asks for `should <action> when <condition>`. Existing tests in
  StatisticsService, Statistics-UI and DatabaseScheduleExecutor use free sentences, and darksouls-progress-bar uses
  both styles. New tests follow the rule; old ones are renamed only when touched.
- The homelab contract (`~/Development/homelab/Homelab Platform Contract — wymagania dla aplikacji.md`) still
  describes SOPS secrets. The apps already use Vault through ExternalSecret, see
  [07-devops-homelab.md](07-devops-homelab.md).
