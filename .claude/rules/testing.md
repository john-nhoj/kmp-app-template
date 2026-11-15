---
paths:
  - "**/commonTest/**"
  - "**/androidHostTest/**"
  - "**/iosTest/**"
  - "konsist/**"
  - "core/testing/**"
---

# Testing

- Tests live in `commonTest` so they run on the JVM (`testAndroidHostTest`) **and** on the iOS
  simulator (`iosSimulatorArm64Test`). Use `androidHostTest` only for JVM-only tools (e.g. Koin `verify()`).
- Framework: `kotlin.test` (`@Test`, `assertEquals`), `kotlinx-coroutines-test` (`runTest`), Turbine
  (`flow.test { awaitItem() }`). No mocking library: write small **fakes** implementing the domain
  interface (private class inside the test, or in `core:testing` once shared by several modules).
- Names: `class <Subject>Test`, test functions in camelCase describing behavior
  (`emitsErrorWhenRepositoryFails`). Avoid backticked names with spaces in `commonTest`: not every target supports them.
- ViewModels: wrap in `runTestWithMainDispatcher { }` (from `:core:testing`) because `viewModelScope`
  uses `Dispatchers.Main`. Assert the full `UiState` sequence with Turbine:
  ```kotlin
  createViewModel().uiState.test {
      assertEquals(XUiState(isLoading = true), awaitItem())
      assertEquals(XUiState(isLoading = false, data = ...), awaitItem())
  }
  ```
- Repositories: pass `StandardTestDispatcher(testScheduler)` as the IO dispatcher.
- Every use case and ViewModel must have a `<Name>Test` (Konsist enforces it). Coverage gate: 80%
  lines overall (Kover, `./gradlew koverVerify`). DI wiring (`*.di.*`) is excluded from coverage.
- Architecture rules live in `konsist/src/test/.../ArchitectureTest.kt`. Add a rule when you introduce
  a new convention; never weaken a rule to make code pass.
