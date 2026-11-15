# CLAUDE.md

Kotlin Multiplatform app: shared Kotlin logic, **native UI on each platform** (Jetpack Compose +
Material 3 on Android, SwiftUI + Apple HIG on iOS). Read `docs/ARCHITECTURE.md` before structural changes.

## Commands

```bash
./gradlew check                       # every quality gate: ktlint, detekt, Konsist, unit tests, Android Lint, Kover ≥ 80%
./gradlew ktlintFormat                # auto-format Kotlin; run before committing
./gradlew testAndroidHostTest         # shared unit tests on JVM (fast feedback)
./gradlew :konsist:test               # architecture rules only
./gradlew :androidApp:assembleDebug   # build the Android app
./gradlew :androidApp:installDebug    # install on a running emulator/device
./gradlew iosSimulatorArm64Test       # shared tests on iOS simulator (macOS only)
```

The development machine may be Linux: iOS compilation and Swift code cannot be built locally there.
Keep Swift changes small and conventional, and say clearly that they still need building in Xcode or in CI.

## Layout

- `feature/<name>/{domain,data,presentation}`: one feature = three KMP modules (convention plugins
  `template.feature.domain|data|presentation` in `build-logic/`).
- `shared/`: composition root (`appModules`, `initKoin`) + iOS framework `Shared` + Swift-facing
  entry points (`AppInitializer`, `ViewModels`, `IosViewModelStoreOwner`) in `iosMain`.
- `androidApp/`: Compose UI per feature in `feature/<name>/`, `navigation/`, `ui/theme/`.
- `iosApp/iosApp/`: SwiftUI per feature in `Features/<Name>/`, helpers in `Core/`.
- `core/testing/`: `runTestWithMainDispatcher` and future shared test utilities.
- `konsist/`: architecture tests. **If one fails, fix the code, not the test.**

## Non-negotiable rules

1. **Dependency direction**: `presentation → domain ← data`. Domain is pure Kotlin (coroutines only).
   Presentation never imports data. Features never import other features (share via `core:*`).
2. **Naming and placement** (enforced by Konsist): `*UseCase` in `domain.usecase` with one public
   `operator fun invoke`; `*Repository` interfaces in `domain.repository`; implementations
   `internal class Default*Repository` in `data.repository`; `*ViewModel` + `*UiState` (data class,
   `val`s only) in `presentation`; Koin modules (`val <name>DataModule`, `val <name>PresentationModule`) in `di`.
3. **Every use case and ViewModel has a `<Name>Test`** in `commonTest`. Use fakes, not mocks.
4. **No user-facing strings in shared code.** Expose data (`platformName: String?`), localize in
   `strings.xml` / `Localizable.xcstrings`.
5. **ViewModels**: a private `MutableStateFlow`, a public `StateFlow<XUiState>`, intents as public
   `fun onX()`. Catch failures at the ViewModel boundary and map them to state; always rethrow
   `CancellationException`.
6. **New shared ViewModel** → register it with `viewModelOf(::X)` in its presentation module, add the
   module to `appModules`, add `export(...)` in `shared/build.gradle.kts`, add
   `ViewModels.x(owner)` in `shared/src/iosMain/.../ViewModels.kt`.
7. **Native look**: Android uses Material 3 components and `MaterialTheme` tokens only (no hard-coded
   colors or sizes in screens). iOS uses standard SwiftUI controls, SF Symbols, system fonts and colors.
8. **Dependencies** go through `gradle/libs.versions.toml`, never as inline coordinates. Build config
   shared by modules goes into a convention plugin, not copy-pasted into `build.gradle.kts`.
9. **Commits** follow Conventional Commits (`feat(home): ...`, `fix(ios): ...`); see `CONTRIBUTING.md`.

## Detailed rules

`.claude/rules/` holds scoped guidance, loaded by path: `kotlin-shared.md`, `android-ui.md`, `ios-ui.md`,
`testing.md`, `networking.md`, `persistence.md`, `build-and-ci.md`. `git.md` (branches, worktrees, PRs)
always applies. The `new-feature` skill scaffolds a whole feature.
