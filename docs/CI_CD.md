# CI/CD

Everything runs on GitHub Actions. Here is the whole flow:

```
feature branch ──PR──▶ CI + Conventional Commits checks ──squash merge──▶ main
                                                                           │
                         release-please keeps a "chore(main): release x.y.z" PR up to date
                                                                           │
                                         merge the release PR ──▶ tag vX.Y.Z + GitHub Release
                                                                           │
                                     (if enabled) Deploy Android ─▶ Google Play internal track
                                     (if enabled) Deploy iOS     ─▶ TestFlight
```

## Workflows

| File | Trigger | Jobs |
|---|---|---|
| `ci.yml` | PR, push to `main` | **Kotlin quality** (ktlint, detekt, Konsist, JVM unit tests, Kover ≥ 80%) · **Android** (Lint, debug APK, unsigned release bundle) · **SwiftLint** · **iOS** (swift-format, shared tests on the iOS simulator, Xcode build) |
| `commitlint.yml` | PR | PR title + every commit follow Conventional Commits |
| `release-please.yml` | push to `main` | Maintains the release PR; on merge: tag, GitHub Release, CHANGELOG, then calls the deploy workflows |
| `deploy-android.yml` | called by release, or manual | Signed AAB → Play internal track (fastlane) |
| `deploy-ios.yml` | called by release, or manual | Signed IPA → TestFlight (fastlane, cloud signing) |

Reports (tests, lint, coverage) and the debug APK are attached to every CI run as artifacts.

## Versioning

- **Version name** (`1.4.0`) comes from Conventional Commits: `fix:` → patch, `feat:` → minor,
  `feat!:` / `BREAKING CHANGE:` → major (minor while < 1.0). release-please writes it to
  `version.txt` (Android `versionName`) and `Config.xcconfig` (iOS `MARKETING_VERSION`).
- **Build number** is computed at deploy time: highest version code on Google Play + 1, latest
  TestFlight build + 1. It never collides and you never edit it by hand.

## Recommended repository settings (do this once)

1. **Settings → General → Pull Requests**: allow only *Squash merging*, with
   *Default commit message = Pull request title*. The PR title becomes the Conventional Commit on `main`.
2. **Settings → Actions → General → Workflow permissions**: enable
   *Allow GitHub Actions to create and approve pull requests* (release-please needs it).
3. **Settings → Branches → Add rule for `main`** (or a ruleset):
   - Require a pull request before merging
   - Require status checks: `Kotlin · lint, architecture, unit tests, coverage`, `Android · lint & build`,
     `iOS · SwiftLint`, `iOS · shared tests & app build`, `PR title`, `Commit messages`
   - Require branches to be up to date
4. **Install the [Renovate GitHub App](https://github.com/apps/renovate)** on the repository. It
   opens a dependency dashboard issue and grouped weekly update PRs (Kotlin + SKIE together,
   AndroidX, Koin, tooling). GitHub Actions minor/patch updates auto-merge when CI is green.
5. *(Optional)* Create a fine-grained PAT (or GitHub App token) with *contents* + *pull requests*
   write access and save it as the `RELEASE_PLEASE_TOKEN` secret. Without it the release PR is
   created by `GITHUB_TOKEN`, which by design does **not** trigger CI on that PR.

## Costs

- Public repositories: Actions minutes are free.
- Private repositories: the free plan includes 2,000 minutes/month; **macOS minutes count 10×**.
  The iOS job typically takes 10–15 minutes (≈ 100–150 billed minutes). To save minutes you can
  run the `ios` job only on `push` to `main`, or only when `iosApp/**`, `shared/**`, `feature/**`
  change (for example with `dorny/paths-filter`).

## Enabling deployments

Deploy jobs are skipped until you turn them on with **repository variables**
(Settings → Secrets and variables → Actions → *Variables*):

| Variable | Value | Effect |
|---|---|---|
| `ANDROID_DEPLOY_ENABLED` | `true` | Release → Google Play internal track |
| `IOS_DEPLOY_ENABLED` | `true` | Release → TestFlight |
| `PLAY_RELEASE_STATUS` | `draft` *(optional)* | Use until the app has been published once on Play |

The secrets each deployment needs, and how to obtain them, are in [DEPLOYMENT.md](DEPLOYMENT.md).
Both deploy jobs use the `production` GitHub **environment**: add *required reviewers* to it if you
want a manual approval before anything reaches the stores.
