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

- `allWarningsAsErrors = true`. Today only StatisticsService sets it; new and touched projects should add it.
- JVM toolchain 21 for JVM code. Current exceptions: DatabaseScheduleExecutor uses 17, and SmartConnectLumBunny
  targets `JVM_11` for Android.
- Versions live in `gradle/libs.versions.toml` and are kept current by Renovate or Dependabot.
