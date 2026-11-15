---
paths:
  - "feature/**/*.kt"
  - "core/**/*.kt"
  - "shared/**/*.kt"
---

# Shared Kotlin (domain / data / presentation)

## Domain (`feature/<name>/domain`)
- Models are immutable `data class`es with domain names (`Greeting`, not `GreetingDto`/`GreetingEntity`).
- Repository interfaces expose `suspend fun` for one-shot work and `Flow<T>` for observable data.
- Use cases: `class VerbNounUseCase(private val repo: XRepository)` with a single
  `suspend operator fun invoke(...)` (or a non-suspend one returning `Flow`). Business rules live here.
- Nothing platform-specific; no Koin, no serialization annotations, no Ktor/Room types.

## Data (`feature/<name>/data`)
- Repository implementations are `internal class Default<Name>Repository(...) : <Name>Repository`.
- Inject `CoroutineDispatcher` with a default (`ioDispatcher: CoroutineDispatcher = Dispatchers.IO`,
  import `kotlinx.coroutines.IO`) so tests can pass a test dispatcher.
- DTOs / entities stay `internal` in data and are mapped to domain models with `toDomain()`
  extension functions in a `mapper` package.
- Platform APIs: declare an `internal fun interface XDataSource` in `commonMain`, plus an
  `internal expect fun deviceXDataSource(): XDataSource` with `actual`s in `androidMain` / `iosMain`
  (see `PlatformInfoDataSource`). Prefer this over `expect class`.
- Koin: `val <name>DataModule = module { single<XRepository> { DefaultXRepository(get()) } }` in `data.di`.

## Presentation (`feature/<name>/presentation`)
```kotlin
class XViewModel(private val getX: GetXUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow(XUiState())
    val uiState: StateFlow<XUiState> = _uiState.asStateFlow()

    fun onRefresh() { /* launch in viewModelScope, update _uiState */ }
}
```
- One `UiState` data class per screen with defaults for the initial state; flags (`isLoading`, `isError`)
  and plain data. For mutually exclusive states a `sealed interface` is fine (SKIE maps it to a Swift
  enum, usable with `switch onEnum(of: state)`).
- One-off events (navigate, show snackbar): model them as state (`val pendingNavigation: Target?`
  plus `fun onNavigationHandled()`), not as `Channel`/`SharedFlow`. It survives configuration changes
  and is trivial to consume from SwiftUI.
- Koin: `val <name>PresentationModule = module { factoryOf(::GetXUseCase); viewModelOf(::XViewModel) }`.
- Exceptions: catch at the ViewModel boundary (with `@Suppress("TooGenericExceptionCaught", "SwallowedException")`
  and a comment), always rethrow `CancellationException`.

## Swift interop (anything reachable from `shared`)
- Keep public API Swift-friendly: no default-argument-only constructors that Swift must call
  (Swift sees every parameter), no generics-heavy signatures, no `inline`/`reified` public functions.
- Top-level functions become `FileNameKt.function()` in Swift; expose Swift entry points as `object`s
  (`AppInitializer.shared.start()`).
- After adding a presentation module: `api(...)` + `export(...)` it in `shared/build.gradle.kts`.
