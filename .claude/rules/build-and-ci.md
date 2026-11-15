---
paths:
  - "build-logic/**"
  - "gradle/**"
  - "**/*.gradle.kts"
  - ".github/**"
  - "fastlane/**"
  - "renovate.json"
  - "release-please-config.json"
---

# Build and CI

## Gradle
- Versions live only in `gradle/libs.versions.toml`. Kotlin, SKIE and the Compose compiler plugin share
  one version: check that SKIE supports a new Kotlin version before bumping (Renovate groups them).
- Module build files stay tiny: apply one convention plugin (`template.feature.domain|data|presentation`,
  `template.kmp.library`, `template.android.application`) and declare dependencies. Targets, SDK levels,
  JVM toolchain, test deps, ktlint/detekt/Kover live in `build-logic/convention/src/main/kotlin`.
- Android namespaces are derived from the Gradle path (`:feature:home:data` → `<basePackage>.feature.home.data`).
  The base package is `app.basePackage` in `gradle.properties`.
- Use type-safe project accessors (`projects.feature.home.domain`), not `project(":...")`, in module scripts.
- New Kotlin module → `include(...)` in `settings.gradle.kts` and, if it has tests, `kover(projects.x)`
  in the root `build.gradle.kts`.
- detekt is pinned to 1.23.x (its embedded Kotlin is forced to 2.0.21 in `QualityConventionPlugin`);
  move to detekt 2.x when it is stable. The Gradle daemon runs on JDK 21 (`gradle/gradle-daemon-jvm.properties`).
- Configuration cache is on: no `project` access at execution time, use providers.

## GitHub Actions
- Job **names** in `ci.yml` and `commitlint.yml` are referenced by branch protection; renaming a job
  means updating the required checks.
- Pin actions to a major version (`@v7`); Renovate updates them.
- Never echo secrets; decode files into `$RUNNER_TEMP`. Deploy jobs are gated by repository variables
  (`ANDROID_DEPLOY_ENABLED`, `IOS_DEPLOY_ENABLED`) and run in the `production` environment.
- The macOS runner is ~10× the cost of Linux. Put anything that doesn't need macOS in a Linux job.

## Releases
- release-please (`release-type: simple`) owns `version.txt`, `CHANGELOG.md` and the
  `MARKETING_VERSION` line in `iosApp/Configuration/Config.xcconfig` (the `x-release-please-version` marker).
  Don't edit those by hand.
- Build numbers come from the stores at deploy time (fastlane): Play max version code + 1,
  latest TestFlight build + 1.
