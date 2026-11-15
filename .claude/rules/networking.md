---
paths:
  - "core/network/**"
  - "feature/*/data/**"
---

# Networking recipe (Ktor), applies when the app first needs HTTP

Not in the skeleton on purpose. Apply it like this so it stays consistent with the architecture.
Check the latest versions on Maven Central first (last checked: Ktor 3.6.x, kotlinx-serialization 1.13.x).

## 1. Version catalog (`gradle/libs.versions.toml`)
```toml
[versions]
ktor = "3.6.1"
[libraries]
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
ktor-client-mock = { module = "io.ktor:ktor-client-mock", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
```

## 2. `core:network` module (shared by all features)
- `core/network/build.gradle.kts`: `template.kmp.library` + `kotlin-serialization` plugin;
  `commonMain`: `api(ktor-client-core)`, content-negotiation, serialization-json, logging, koin-core;
  `androidMain`: `ktor-client-okhttp`; `iosMain`: `ktor-client-darwin`. Include it in `settings.gradle.kts`.
- `HttpClientFactory.kt`: `internal fun createHttpClient(engine: HttpClientEngine, baseUrl: String): HttpClient`
  installing `ContentNegotiation { json(Json { ignoreUnknownKeys = true }) }`, `defaultRequest { url(baseUrl) }`,
  `HttpTimeout`, `Logging` (debug only), and `expectSuccess = true`.
- Engine per platform via `internal expect fun platformEngine(): HttpClientEngine` (OkHttp / Darwin).
- `val networkModule = module { single { createHttpClient(platformEngine(), BASE_URL) } }` in `core.network.di`;
  add it to `appModules` in `:shared`.
- Android: add `<uses-permission android:name="android.permission.INTERNET" />` to the manifest.

## 3. In a feature's data module
```
data/
  remote/XRemoteDataSource.kt     internal class, takes HttpClient, returns DTOs
  remote/dto/XDto.kt              @Serializable internal data class (wire names via @SerialName)
  mapper/XMapper.kt               internal fun XDto.toDomain(): X
  repository/DefaultXRepository   calls the data source, maps to domain, maps errors
```
- Add the `kotlin-serialization` plugin and `implementation(projects.core.network)` to that data module.
- Never let DTOs, `HttpClient` or Ktor exceptions leave the data module. Translate failures into
  domain errors (e.g. a `sealed class XError : Exception()` in domain, or `Result<T>`).
- Ktor calls are already main-safe; use `withContext(ioDispatcher)` only around blocking work.

## 4. Tests
- `commonTest` with `MockEngine` (`ktor-client-mock`): build the client with
  `createHttpClient(MockEngine { respond(json, headers = headersOf("Content-Type", "application/json")) }, "https://test")`
  and assert both the request (path, query) and the mapped domain result.
- Consider a Konsist rule that `..dto..` classes are `internal`.

## Base URLs per environment
Start with a constant. For dev/staging/prod, add the BuildKonfig Gradle plugin
(`com.codingfeline.buildkonfig`) in `core:network`, or pass the URL from each app into `initKoin`
via Koin properties.
