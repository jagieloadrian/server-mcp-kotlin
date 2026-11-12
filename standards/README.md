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
| [02-git-workflow.md](general/02-git-workflow.md) | Branches, commits, verification, pushing, PROJECT-STATUS.md |
| [03-testing.md](general/03-testing.md) | What to test, test first (TDD), naming, given/when/then, mocking, real dependencies, coverage, checklist |
| [04-backend.md](general/04-backend.md) | Package by feature, config, validation, errors, database, observability, messaging, identity and roles (OIDC) |
| [05-ui-ux.md](general/05-ui-ux.md) | Typography, colour and contrast, spacing, states, forms, content, accessibility, release checklist |
| [06-devops-homelab.md](general/06-devops-homelab.md) | Images, Helm/Flux deploy, runtime contract, secrets, local compose |
| [07-ai-assistants.md](general/07-ai-assistants.md) | Rules for Claude and other agents working in these repos |
| [08-sdd-speckit-flow.md](general/08-sdd-speckit-flow.md) | Spec-kit command order, analyze rounds, branches |
| [09-agents.md](general/09-agents.md) | Delegating to subagents, checking their output, project setup, building agent apps |

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

## Sources

- Testing and UI: `~/Documents/AI/specs/TEST_SPECIFICATION.md` and `~/Documents/AI/Czytelny Interfejs.md`.
- Kotlin agent skill packs, taken where they do not contradict the rules above (each commit body names what was
  left out and why):
  - [Kotlin/kotlin-agent-skills](https://github.com/Kotlin/kotlin-agent-skills) (JetBrains): JPA entity mapping,
    Java to Kotlin, Kotlin Toolchain.
  - [lazyskills.sh/skills/kotlin](https://lazyskills.sh/skills/kotlin): ECC kotlin-patterns, kotlin-testing,
    kotlin-ktor-patterns, kotlin-exposed-patterns; awesome-copilot kotlin-springboot; chrisbanes
    kotlin-concurrency-and-flow; apollographql apollo-kotlin.
  - Not taken: KMP iOS skills (CocoaPods to SPM, Kotlin/Native build performance), AGP 9 and
    kotlinx.collections.immutable migrations.

## Precedence

1. An explicit request in the current task.
2. The project's `.specify/memory/constitution.md` and `CLAUDE.md`.
3. These files.
4. The language's official conventions, for Kotlin the
   [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).
