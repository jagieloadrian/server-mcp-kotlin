# Spring Boot

Applies to Kotlin projects on Spring Boot. Framework-neutral backend rules:
[../general/04-backend.md](../general/04-backend.md). JPA: [05-persistence.md](05-persistence.md).

## Build

- `kotlin("plugin.spring")` so Spring stereotypes are open. With JPA also `kotlin("plugin.jpa")`
  ([05-persistence.md](05-persistence.md)).
- Starters as usual. Versions in `gradle/libs.versions.toml`, Spring versions from the Spring Boot BOM.

## Structure

- Package by feature ([../general/04-backend.md](../general/04-backend.md), Layout).
- Constructor injection with `private val` parameters. No field injection, no `lateinit` beans.
- `@RestController` stays thin, `@Service` classes are stateless and hold the rules, `@Repository` or Spring Data
  interfaces hold the queries.

## Configuration

- `application.yml` with values from env vars. Profiles (`application-local.yml`) for local runs.
- Typed config: `@ConfigurationProperties` on a `data class`.
- Secrets come from env vars or Vault, never from a committed file.

## Web

- DTOs are `data class`es. Controllers never return entities.
- Bean Validation (JSR 380) with `@Valid` on the request body and use-site targets on DTO properties:
  `@field:NotBlank`, `@field:Size(max = 100)`. A bare `@NotBlank` lands on the constructor parameter and is
  not checked.
- One `@RestControllerAdvice` with `@ExceptionHandler`s maps errors to the shared error body.

## Security

- `spring-boot-starter-oauth2-resource-server` with `spring.security.oauth2.resourceserver.jwt.issuer-uri` and
  `audiences`.
- A `JwtAuthenticationConverter` whose `JwtGrantedAuthoritiesConverter` reads `groups` (`authoritiesClaimName =
  "groups"`, `authorityPrefix = "ROLE_"`). Routes check roles with `hasRole`.

## Transactions

- `@Transactional` on service classes or functions, never on controllers.
- With open-in-view disabled, map lazy associations to DTOs inside the transaction.

## Coroutines

- `suspend` controller and service functions are allowed. [02-coroutines.md](02-coroutines.md) applies.

## Logging

- kotlin-logging as in [01-language.md](01-language.md), not `LoggerFactory` in a companion object.

## Testing

- Slices first: `@WebMvcTest` for controllers, `@DataJpaTest` for repositories. `@SpringBootTest` only for flows
  that need the whole context.
- Testcontainers for the database. Kotest assertions and MockK as in [07-testing.md](07-testing.md); Spring beans
  are replaced with springmockk `@MockkBean`.
