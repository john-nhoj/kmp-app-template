---
name: new-feature
description: Build a feature end to end in its own git worktree (domain, data, presentation KMP modules, Koin wiring, Android Compose screen, iOS SwiftUI view, tests) following this repo's clean architecture, optionally from a GitHub issue. Use when the user asks to add a feature, screen or module, or to implement a GitHub issue (`#3`, an issue URL).
---

# New feature

Input: a GitHub issue (`#3`, `3` or an issue URL) **or** a feature name (e.g. `profile`) and what
the screen does. Below, `<name>` = lowercase (`profile`), `<Name>` = PascalCase (`Profile`),
`<base>` = `app.basePackage` from `gradle.properties`, `<repo>` = the main checkout's directory name,
`<wt>` = the worktree's absolute path.
Use `feature/home` as the reference implementation for every file and copy its style.

## 0. Issue, branch and worktree
Every feature is built in its own git worktree, never in the main checkout.

1. **Read the issue** when there is one:
   `gh issue view <n> --json number,title,body,labels,comments`. Its acceptance criteria become
   the scope, the tests (one ViewModel/use case assertion per criterion where possible) and the PR's
   "How to test". Ask before guessing if the issue is vague. Don't comment on, assign or label the
   issue unless the user asks.
2. **Scope**: decide whether this is a new feature (new `feature/<name>` modules, steps 1–8) or an
   extension of an existing one (e.g. a new action on the home screen belongs in `feature/home`: add
   use cases, state and UI there and skip the steps that don't apply). Say which, and why, before writing code.
3. **Branch**: `feat/<n>-<slug>` for an issue (`feat/3-logout`), else `feat/<name>`. Kebab-case.
4. **Base**: `origin/main` by default (`git fetch origin` first). If the work needs code that only
   exists on an open PR's branch (`gh pr list --state open`, e.g. auth before profile), base on that
   branch instead, target the PR at it later, and tell the user it's stacked on #<pr>.
5. **Already in a worktree?** If `git rev-parse --git-dir` differs from `--git-common-dir` and the
   branch is a feature branch (e.g. a Herdr-delegated agent was started there), use it and skip to 1.
6. **Create the worktree**:
   - In Herdr (`HERDR_ENV=1`): `herdr worktree create --cwd <repo root> --branch <branch> --base <base> --label <slug> --no-focus`.
     Herdr chooses the location (`~/.herdr/worktrees/<repo>/<branch with / → ->`); read
     `.result.worktree.path` and `.result.workspace.workspace_id` from the JSON, don't predict them.
   - Otherwise: `git worktree add -b <branch> ../<repo>-worktrees/<branch with / → -> <base>`.
   - Never nest a worktree inside the repo: Konsist's `scopeFromProduction()` scans every `.kt` file
     under the project dir and would pick up its copies.
7. **Work only in `<wt>`**: Read/Edit/Write with absolute `<wt>/...` paths and prefix shell commands
   with `cd <wt> &&` (the session's cwd stays in the main checkout). A stray edit in the main checkout
   lands on the wrong branch. Gitignored files are not copied: if the main checkout has
   `local.properties`, copy it to `<wt>`, otherwise prefix Gradle with `ANDROID_HOME=<sdk>`.

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

## 9. Verify (in `<wt>`)
```bash
cd <wt> && ./gradlew ktlintFormat check :androidApp:assembleDebug
```
All Konsist rules must pass without editing `konsist/`. The first build in a fresh worktree compiles
everything, so expect it to be slower. To try it on a device, run
`cd <wt> && ./gradlew :androidApp:installDebug`: the app ID is shared, so installing from a worktree
replaces the app built from the main checkout or another worktree.

## 10. Finish
- Commit on the worktree branch: `feat(<name>): <what the user can now do>`, with a `Refs: #<n>`
  footer for an issue.
- Push and open a PR only when the user asks: Conventional Commit title, `.github/pull_request_template.md`
  filled in, `Closes #<n>` in "What & why", `--base <base branch>` when stacked.
- Report `<wt>`, the branch, the base, and (in Herdr) the workspace ID. Leave the worktree in place;
  remove it only when the user asks (`herdr worktree remove --workspace <id>` or `git worktree remove <wt>`).
