---
name: new-feature
description: Scaffold a new feature end to end (domain, data, presentation KMP modules, Koin wiring, Android Compose screen, iOS SwiftUI view, tests) following this repo's clean architecture. Use when the user asks to add a feature, screen or module.
---

# New feature

Input: a feature name (e.g. `profile`) and what the screen does. Below, `<name>` = lowercase
(`profile`), `<Name>` = PascalCase (`Profile`), `<base>` = `app.basePackage` from `gradle.properties`.
Use `feature/home` as the reference implementation for every file and copy its style.

## 1. Modules
Create the files and `include(...)` the modules in `settings.gradle.kts`:
- `feature/<name>/domain/build.gradle.kts`: `plugins { alias(libs.plugins.template.feature.domain) }`
- `feature/<name>/data/build.gradle.kts`: `template.feature.data` + `commonMain { implementation(projects.feature.<name>.domain) }`
- `feature/<name>/presentation/build.gradle.kts`: `template.feature.presentation` + `commonMain { api(projects.feature.<name>.domain) }`

## 2. Domain (`<base>.feature.<name>.domain`)
- `model/<Model>.kt`: immutable data classes, no user-facing strings
- `repository/<Name>Repository.kt`: interface
- `usecase/<Verb><Noun>UseCase.kt`: one public `operator fun invoke`

## 3. Data (`<base>.feature.<name>.data`)
- `repository/Default<Name>Repository.kt`: `internal`, injectable `ioDispatcher`
- `datasource/...`: data sources (`expect`/`actual` for platform APIs; Ktor/Room per `.claude/rules/networking.md` / `persistence.md`)
- `di/<Name>DataModule.kt`: `val <name>DataModule = module { single<<Name>Repository> { Default<Name>Repository(...) } }`

## 4. Presentation (`<base>.feature.<name>.presentation`)
- `<Name>UiState.kt`: data class, `val`s with defaults
- `<Name>ViewModel.kt`: private `MutableStateFlow`, public `StateFlow`, `fun onX()` intents
- `di/<Name>PresentationModule.kt`: `factoryOf(::UseCase)` + `viewModelOf(::<Name>ViewModel)`

## 5. Tests (`commonTest` in each module)
`<UseCase>Test`, `Default<Name>RepositoryTest` (fake data source + `StandardTestDispatcher(testScheduler)`),
`<Name>ViewModelTest` (`runTestWithMainDispatcher` + Turbine, assert the full state sequence, success and failure).

## 6. Shared wiring (`shared/`)
- `build.gradle.kts`: `api(projects.feature.<name>.presentation)`, `implementation(projects.feature.<name>.data)`,
  `export(projects.feature.<name>.presentation)` in the framework block
- `AppModules.kt`: add `<name>DataModule`, `<name>PresentationModule` to `appModules`
- `iosMain/.../ViewModels.kt`: `fun <name>(owner: IosViewModelStoreOwner): <Name>ViewModel = owner.resolve()`
- root `build.gradle.kts`: `kover(projects.feature.<name>.domain|data|presentation)`

## 7. Android (`androidApp/src/main/kotlin/<base path>/feature/<name>/<Name>Screen.kt`)
- `<Name>Route` (koinViewModel + collectAsStateWithLifecycle) + stateless `<Name>Screen` + `@PreviewLightDark` previews
- strings in `res/values/strings.xml` (`<name>_...`)
- `navigation/Route.kt`: `@Serializable data object <Name> : Route` (or data class with args);
  `AppNavigation`: `entry<Route.<Name>> { <Name>Route(...) }` and a navigation lambda from the caller
- Follow `.claude/rules/android-ui.md` (Material 3 only)

## 8. iOS (`iosApp/iosApp/Features/<Name>/<Name>View.swift`)
- `<Name>View` (owns `SharedViewModel { ViewModels.shared.<name>(owner: $0) }`, observes `uiState` in `.task`)
  + stateless `<Name>Content` + `#Preview`s. No `project.pbxproj` edits (synchronized folders).
- Link it from the calling view (`NavigationLink(value:)` + `.navigationDestination`).
- Follow `.claude/rules/ios-ui.md` (HIG, SF Symbols). On Linux, say the Swift part still needs building in Xcode or in CI.

## 9. Verify
```bash
./gradlew ktlintFormat check :androidApp:assembleDebug
```
All Konsist rules must pass without editing `konsist/`. Suggested commit: `feat(<name>): <what the user can now do>`.
