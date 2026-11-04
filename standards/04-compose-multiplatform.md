# Compose Multiplatform

Applies to Statistics-UI (wasmJs, js, JVM desktop) and SmartConnectLumBunny (Android + desktop JVM). Visual and
UX rules are in [09-ui-ux.md](09-ui-ux.md).

## Shared code first

- Anything not inherently platform-specific lives in `commonMain`.
- Platform behaviour is reached only through `expect` / `actual`. Platform types never leak into `commonMain`,
  and platform source sets hold only the `actual` side.
- Every shared feature builds and runs on every target of the project.

## UI layer

- Unidirectional and thin. Composables render state and send events; state holders or ViewModels own the logic.
- No HTTP, BLE, storage or other I/O inside composables.
- Form rules live in the `validation` package and mirror the backend rules (for example the identity rule for
  pub/sub sources).
- Fixed values such as roles and definition kinds are constants, not string literals in screens.
- API calls go through one client (`PlatformApi`) that handles sign-in and errors once.

## Dependency injection

- Koin with the Koin Compiler Plugin (`io.insert-koin.compiler.plugin`). Never KSP for Koin.
- Annotations come from `org.koin.core.annotation.*`.
- Load annotated modules with `module<AppModule>()` from `org.koin.plugin.module.dsl`, called directly inside
  `startKoin { }` / `koinApplication { }`, not inside `modules(...)`.
- Every Gradle module that makes this call applies the plugin.

## Networking

- The Ktor client engine is chosen per platform: CIO on desktop, fetch / Js on the web. CIO needs Node sockets
  and does not work in the browser.
- The backend URL comes from BuildKonfig: `-PbackendUrl=`. An empty value means same origin.

## Build notes

- With the `com.android.kotlin.multiplatform.library` plugin (AGP 9), task names differ from classic AGP:
  `compileAndroidMain`, `testAndroidHostTest`, `connectedAndroidDeviceTest`. Check with `:shared:tasks --all`.
- Keep Konsist in a JVM-only test source set, never in `commonTest` or device tests.
- ktlint exempts `@Composable` functions from function naming.

## Web image

- `nginxinc/nginx-unprivileged`, UID 10001, a COPY-only Dockerfile (multi-arch buildx without QEMU), JSON access
  log, `/health/live` and `/health/ready` locations.

## Running

- Run the web target with `./gradlew :composeApp:wasmJsBrowserDevelopmentRun` (port 8081).
- Never start the Statistics-UI desktop app from a terminal or agent session (`:composeApp:run`, `jvmRun`,
  `hotRunJvm`, `runDistributable`, also under Xvfb). It crashes the Xorg session.
