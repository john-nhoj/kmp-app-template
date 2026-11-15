---
name: new-feature
description: Build a feature end to end in its own git worktree (domain, data, presentation KMP modules, Koin wiring, Android Compose screen, iOS SwiftUI view, tests) following this repo's clean architecture, optionally from a GitHub issue. In Herdr, it creates the worktree and delegates the build to a Claude agent there. Use when the user asks to add a feature, screen or module, or to implement a GitHub issue (`#3`, an issue URL).
---

# New feature

Input: a GitHub issue (`#3`, `3` or an issue URL) **or** a feature name (e.g. `profile`) and what
the screen does. Below, `<name>` = lowercase (`profile`), `<Name>` = PascalCase (`Profile`),
`<base>` = `app.basePackage` from `gradle.properties`, `<repo>` = the main checkout's directory name,
`<wt>` = the worktree's absolute path.
Use `feature/home` as the reference implementation for every file and copy its style.

## 0. Issue, branch and worktree
Every feature is built in its own git worktree, never in the main checkout. In Herdr
(`HERDR_ENV=1`) the session that receives the request only sets up the worktree and delegates the
work to a new Claude agent there (0b); otherwise it builds the feature itself (0c).

### 0a. Already in a feature worktree?
If `git rev-parse --git-dir` differs from `--git-common-dir` and the branch is a feature branch
(e.g. you are the agent delegated in 0b), you are `<wt>`: skip 0b, do 0c steps 1, 2 and 4, then
sections 1–10. Don't create another worktree and don't delegate again.

### 0b. Branch, base and worktree
1. **Branch**: `feat/<n>-<slug>` for an issue (`feat/3-logout`), else `feat/<name>`. Kebab-case.
   For the slug, `gh issue view <n> --json title` is enough; in Herdr, leave reading and analysing
   the issue to the delegated agent.
2. **Base**: `origin/main` by default (`git fetch origin` first). If the work needs code that only
   exists on an open PR's branch (`gh pr list --state open`, e.g. auth before profile), base on that
   branch instead, target the PR at it later, and tell the user it's stacked on #<pr>.
3. **Create the worktree**:
   - In Herdr: load the `herdr` skill, then
     `herdr worktree create --cwd <repo root> --branch <branch> --base <base> --label <slug> --no-focus`.
     Herdr chooses the location (`~/.herdr/worktrees/<repo>/<branch with / → ->`); read
     `.result.worktree.path` and `.result.workspace.workspace_id` from the JSON, don't predict them.
   - Otherwise: `git worktree add -b <branch> ../<repo>-worktrees/<branch with / → -> <base>`.
   - Never nest a worktree inside the repo: Konsist's `scopeFromProduction()` scans every `.kt` file
     under the project dir and would pick up its copies.
4. **Gitignored files are not copied**: if the main checkout has `local.properties`, copy it to
   `<wt>`; otherwise Gradle in `<wt>` needs an `ANDROID_HOME=<sdk>` prefix.
5. **In Herdr, delegate** (don't build the feature in this session):
   - Root pane: `.result.root_pane.pane_id` from the create response, else the only pane in
     `herdr pane list --workspace <workspace id>`. If its shell isn't in `<wt>`, `cd` there first.
   - `herdr agent start <agent> --kind claude --pane <pane id>`, where `<agent>` is unique and
     lowercase: `<repo>-<n>` for an issue, else `<repo>-<name>`.
   - `herdr agent prompt <agent> "<prompt>"` (no `--wait`). The prompt must stand on its own:
     > Use the new-feature skill to implement GitHub issue #<n> (or: the `<name>` feature: <what the
     > user asked for, verbatim>). You are in its worktree `<wt>` on branch `<branch>`, based on
     > `<base>` (stacked on #<pr>, if so). Read the issue, decide the scope, implement, test with
     > `./gradlew ktlintFormat check :androidApp:assembleDebug`, commit, and report back: what
     > changed, test status, and open questions. Ask me if the issue is vague. <Push and open a PR
     > only if the user asked for one; say so here.>
   - Report `<wt>`, the workspace ID and the agent name, then **stop**. Don't wait for the agent
     unless the user asked you to keep track; if they did, poll with
     `herdr agent wait <agent> --timeout ...`, check with `herdr agent read <agent> --source recent-unwrapped`,
     bring any `blocked` question or approval prompt to the user (never answer it yourself), and
     summarize the result when it is `done`/`idle`. Never close the workspace, worktree or agent
     unless asked.

### 0c. Build it (outside Herdr, or as the delegated agent)
1. **Read the issue** when there is one:
   `gh issue view <n> --json number,title,body,labels,comments`. Its acceptance criteria become
   the scope, the tests (one ViewModel/use case assertion per criterion where possible) and the PR's
   "How to test". Ask before guessing if the issue is vague. Don't comment on, assign or label the
   issue unless the user asks.
2. **Scope**: decide whether this is a new feature (new `feature/<name>` modules, sections 1–8) or
   an extension of an existing one (e.g. a new action on the home screen belongs in `feature/home`:
   add use cases, state and UI there and skip the sections that don't apply). Say which, and why,
   before writing code.
3. **Create the worktree** as in 0b steps 1–4 (non-Herdr variant) unless 0a applies.
4. **Work only in `<wt>`**: Read/Edit/Write with absolute `<wt>/...` paths and prefix shell commands
   with `cd <wt> &&` when the session's cwd is the main checkout. A stray edit in the main checkout
   lands on the wrong branch.

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
- Report `<wt>`, the branch, the base, what changed, test status and open questions. Leave the worktree in place;
  remove it only when the user asks (`herdr worktree remove --workspace <id>` or `git worktree remove <wt>`).
