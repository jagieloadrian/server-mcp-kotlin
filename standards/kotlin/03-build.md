# Kotlin build

## Gradle

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

- `allWarningsAsErrors = true`, new and touched projects should add it.
- JVM toolchain 21 for JVM code.
- Versions live in `gradle/libs.versions.toml` and are kept current by Renovate or Dependabot.

## Kotlin Toolchain

JetBrains' declarative build (formerly Amper, `project.yaml` / `module.yaml`, `./kotlin` wrapper).

- New projects use Gradle. The Toolchain applies only where a project already uses it; there, do not propose
  moving back to Gradle. Moving a Gradle project to the Toolchain only on explicit request.
- Run `./kotlin` from the project root. Never call a global binary when the wrapper exists.
- One `module.yaml` per module (`product: jvm/app`, `jvm/lib`, `lib` for multiplatform). No `${...}`
  interpolation. Shared settings live in `*.module-template.yaml` files listed under `apply:`.
- Versions in `libs.versions.toml`, referenced as `$libs.<key>`.
- No linters are bundled. ktlint, coverage and other checks are local-plugin tasks under `checks:`, and the gate
  is `kotlin check` instead of `./gradlew check build`.
- Gradle plugins cannot be used. Reimplement the behaviour as a local plugin; a build-time step such as code
  generation becomes a local plugin, never hand-written generated code.
- Multiplatform code lives in `src@jvm/`, `src@android/`, `src@ios/`; dependencies take the same qualifier
  (`dependencies@android:`).
