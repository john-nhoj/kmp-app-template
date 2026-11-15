# Template: Kotlin Multiplatform app (Android + iOS)

A starting point for native mobile apps that share their business logic in Kotlin and keep a
**100% native UI** on each platform:

| | Android | iOS |
|---|---|---|
| UI | Jetpack Compose + Material 3 (dynamic color, edge-to-edge, Navigation 3) | SwiftUI (NavigationStack, SF Symbols, String Catalogs) |
| Shared (Kotlin) | domain · data · presentation (ViewModels) | same code, packaged as the `Shared` framework (with SKIE) |
| Min OS | Android 8.0 (API 26) | iOS 17 |

## Quick start

```bash
# 1. Create your repo from this template (GitHub "Use this template"), clone it, then:
./scripts/rename.sh com.yourcompany.yourapp "Your App"

# 2. Android: run on a connected device / emulator (Linux, macOS or Windows)
./gradlew :androidApp:installDebug

# 3. Everything CI checks on Linux: lint, architecture rules, unit tests, coverage
./gradlew check
```

**iOS** (macOS only): open `iosApp/iosApp.xcodeproj` in Xcode and press ▶. The Kotlin framework is
built automatically by an Xcode build phase.

You do **not** need to install a JDK: Gradle downloads JDK 21 on first run if it is missing.
Full setup for both machines: [docs/SETUP.md](docs/SETUP.md).

## Project structure

```
├── androidApp/              Android app: Compose UI, theme, navigation, Application class
├── iosApp/                  Xcode project: SwiftUI views, assets, Config.xcconfig (app identity)
├── shared/                  Composition root (Koin) + iOS framework "Shared"
├── feature/<name>/
│   ├── domain/              Pure Kotlin: models, repository contracts, use cases
│   ├── data/                Repository implementations, data sources, expect/actual
│   └── presentation/        Shared ViewModels + UiState (used by Compose and SwiftUI)
├── core/testing/            Test helpers shared by every module
├── konsist/                 Architecture tests (clean-architecture rules as unit tests)
├── build-logic/             Gradle convention plugins (one place for build config)
├── config/detekt/           Static analysis rules
├── fastlane/                Store deployment lanes (Play internal track, TestFlight)
├── .github/workflows/       CI, Conventional Commits check, releases, deployments
└── docs/                    Architecture, setup, CI/CD and store deployment guides
```

## Everyday commands

| Command | What it does |
|---|---|
| `./gradlew :androidApp:installDebug` | Build & install the Android debug app |
| `./gradlew check` | All quality gates (ktlint, detekt, Konsist, unit tests, Android Lint, Kover ≥ 80%). On macOS it also runs the shared tests on the iOS simulator |
| `./gradlew ktlintFormat` | Auto-format Kotlin |
| `./gradlew testAndroidHostTest` | Shared unit tests on the JVM (fast) |
| `./gradlew iosSimulatorArm64Test` | Shared unit tests on the iOS simulator (macOS) |
| `./gradlew koverHtmlReport` | Coverage report → `build/reports/kover/html/index.html` |
| `lefthook install` | Install git hooks (commit message check, formatting, pre-push checks) |

## Documentation

- [Architecture](docs/ARCHITECTURE.md): layers, module graph, how Android/iOS consume shared code, adding a feature
- [Setup](docs/SETUP.md): Linux (Android) and macOS (iOS) machines, git hooks
- [CI/CD](docs/CI_CD.md): what each workflow does, branch protection, release flow, costs
- [Store deployment](docs/DEPLOYMENT.md): step-by-step Google Play and TestFlight setup
- [Contributing](CONTRIBUTING.md): Conventional Commits, branches, pull requests
- [CLAUDE.md](CLAUDE.md) and [.claude/](.claude/): conventions for AI-assisted development
