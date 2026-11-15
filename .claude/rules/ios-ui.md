---
paths:
  - "iosApp/**"
---

# iOS UI (SwiftUI + Apple Human Interface Guidelines)

Follow the [Human Interface Guidelines](https://developer.apple.com/design/human-interface-guidelines).
Minimum iOS 17 (Observation, `ContentUnavailableView`, `NavigationStack`, String Catalogs are available).
SwiftLint (`.swiftlint.yml`, strict) and swift-format (`.swift-format`) run in CI.

**Swift cannot be compiled on Linux.** When working from a Linux machine, keep Swift edits minimal
and idiomatic, double-check Kotlin→Swift names (below), and state that the change still needs building in Xcode or in CI.

## Files
- The Xcode project uses synchronized folders: create files under `iosApp/iosApp/` and they are
  compiled automatically. **Never edit `project.pbxproj` to add files.**
- One feature per folder: `Features/<Name>/<Name>View.swift`. Shared helpers in `Core/`.
- App identity / version: `iosApp/Configuration/Config.xcconfig` (not the project file).

## Screen structure
```swift
struct XView: View {                       // stateful: owns the shared ViewModel
    @State private var viewModel = SharedViewModel { ViewModels.shared.x(owner: $0) }
    @State private var uiState = XUiState(/* every parameter, Swift has no Kotlin defaults */)

    var body: some View {
        XContent(uiState: uiState, onRefresh: { viewModel.instance.onRefresh() })
            .task { for await state in viewModel.instance.uiState { uiState = state } }
    }
}

struct XContent: View { /* stateless, previewable: let uiState, closures for intents */ }

#Preview("Content") { XContent(uiState: XUiState(...), onRefresh: {}) }
```
- Never call Koin or `ViewModels` from `XContent` or previews (Koin is not started in previews).
- `.task {}` is cancelled automatically when the view disappears; `SharedViewModel` clears the Kotlin
  ViewModel (and its coroutines) on deinit.

## Kotlin → Swift cheatsheet (with SKIE)
| Kotlin | Swift |
|---|---|
| `object Foo { fun bar() }` | `Foo.shared.bar()` |
| `StateFlow<T>` / `Flow<T>` | `for await value in flow { }` (`AsyncSequence`) |
| `suspend fun load(): T` | `try await load()` |
| `sealed interface State` | `switch onEnum(of: state) { case .loading: ... case .content(let c): ... }` |
| `val isLoading: Boolean` | `isLoading` (`Bool`) |
| `String?` / `List<T>` | `String?` / `[T]` |
| `Int` / `Long` | `Int32` / `Int64` |
| data class constructor | `XUiState(a:b:c:)` with **all** parameters |
| top-level `fun f()` in `Foo.kt` | `FooKt.f()` |

## Apple design
- Navigation: `NavigationStack` with `.navigationTitle` (large titles by default), `NavigationLink(value:)`
  + `.navigationDestination(for:)`. Toolbar actions in `.toolbar { ToolbarItem(placement: .primaryAction) }`.
- Standard controls only: `List`, `Form`, `Button` (`.borderedProminent` for the primary action), `Toggle`,
  `Picker`, `ProgressView`, `ContentUnavailableView` for empty/error states, `.refreshable` for pull-to-refresh,
  `.searchable` for search, `.sheet`/`.confirmationDialog` for modals.
- SF Symbols for icons (`Label("Title", systemImage: "...")`), system fonts (`.font(.headline)`),
  semantic colors (`.primary`, `.secondary`, `Color.accentColor`, `.background`). Support Dynamic Type and
  dark mode for free by never hard-coding font sizes or colors.
- Strings: write literals in views (`Text("Hello, \(name)!")`). They are `LocalizedStringKey`s and
  Xcode extracts them into `Localizable.xcstrings`. Add translations there.
- Accessibility: icon-only buttons use `Button("Refresh", systemImage: "arrow.clockwise")` so VoiceOver
  reads the title; add `.accessibilityLabel` where the visible text is not enough.
