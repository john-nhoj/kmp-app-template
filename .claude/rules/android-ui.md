---
paths:
  - "androidApp/**"
---

# Android UI (Jetpack Compose + Material 3)

Follow [Material Design 3](https://m3.material.io) and the
[Compose API guidelines](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md).
detekt/ktlint enforce the Compose rules (io.nlopez.compose.rules).

## Screen structure (one file per screen: `feature/<name>/<Name>Screen.kt`)
- `@Composable fun <Name>Route(modifier: Modifier = Modifier, viewModel: XViewModel = koinViewModel())`:
  collects `viewModel.uiState.collectAsStateWithLifecycle()` and passes state + lambdas down.
- `@Composable fun <Name>Screen(uiState: XUiState, onX: () -> Unit, modifier: Modifier = Modifier)`:
  stateless, previewable. Never pass the ViewModel below the Route.
- `private` sub-composables for sections; every composable that emits UI takes `modifier: Modifier = Modifier`
  as the first optional parameter and applies it to its root.
- Previews: `@PreviewLightDark` for each meaningful state (content, loading, error, empty), wrapped
  in `TemplateTheme { }`, as `private fun XPreview()`.

## Material 3
- Screens use `Scaffold` + a top app bar (`TopAppBar` / `MediumTopAppBar` / `LargeTopAppBar`) with
  `TopAppBarDefaults.*ScrollBehavior` and `Modifier.nestedScroll(...)`.
- Only M3 components (`androidx.compose.material3.*`); never `androidx.compose.material.*` widgets.
  Icons: `Icons.Filled/Outlined/Rounded.*` (material-icons-core; add `material-icons-extended` only if needed).
- Colors, type, shapes come from `MaterialTheme.colorScheme / typography / shapes`. No hard-coded
  `Color(0x...)` outside `ui/theme/`. Spacing in multiples of 4.dp (8, 12, 16, 24).
- Dynamic color is on by default (Android 12+); brand colors are the fallback in `ui/theme/Color.kt`.
- Edge-to-edge is enabled: respect `Scaffold` `innerPadding` / `WindowInsets`.
- Loading: `CircularProgressIndicator` / `LinearProgressIndicator`. Feedback: `SnackbarHost` in the
  Scaffold. Pull-to-refresh: `PullToRefreshBox`. Lists: `LazyColumn` with stable `key`s.
- Adaptive layouts (tablets/foldables): use `material3-adaptive` (`NavigationSuiteScaffold`,
  `ListDetailPaneScaffold`) when the app grows beyond a single pane.

## Strings, accessibility
- All text in `res/values/strings.xml` (`stringResource(R.string.x)`), named `<feature>_<what>`
  or `action_<verb>`. Formatting with `%1$s` placeholders.
- Every `Icon` in a clickable has a `contentDescription` (string resource); decorative icons use `null`.
- Touch targets ≥ 48.dp (M3 components already comply).

## Navigation (Navigation 3)
- Destinations are `@Serializable` entries of `sealed interface Route : NavKey` in `navigation/Route.kt`.
  Arguments are properties: `@Serializable data class Detail(val id: String) : Route`.
- Register each screen in `AppNavigation`'s `entryProvider { entry<Route.X> { key -> XRoute(...) } }`.
- Navigate by mutating the back stack: pass lambdas like `onOpenDetail = { id -> backStack.add(Route.Detail(id)) }`
  from `AppNavigation` into Routes; screens never touch the back stack directly.
- ViewModels needing arguments: `koinViewModel { parametersOf(key.id) }` and
  `viewModel { (id: String) -> DetailViewModel(id, get()) }` in Koin.
