# Setup

## Any machine: Java

Nothing to install. `gradle/gradle-daemon-jvm.properties` pins Gradle to **JDK 21** and Gradle
downloads it automatically (foojay) the first time, even if your system Java is a JRE or a newer
version. Any `java` ≥ 17 on the `PATH` is enough to start the wrapper.

## Linux: Android development

1. Install **Android Studio** (recommended, for emulators, previews and debugging) or only the
   command-line SDK. Android Studio: install the *Kotlin Multiplatform* plugin
   (Settings → Plugins) for KMP run configurations and `expect`/`actual` navigation.
2. Point Gradle to the SDK: Android Studio writes `local.properties` for you; otherwise create it:
   ```properties
   sdk.dir=/home/<you>/Android/Sdk
   ```
3. Run:
   ```bash
   ./gradlew :androidApp:installDebug          # on a running emulator / USB device
   # or open the project in Android Studio and run the "androidApp" configuration
   ```

> iOS tasks are skipped automatically on Linux (`kotlin.native.ignoreDisabledTargets=true`).

## macOS: iOS development

1. Install **Xcode** from the App Store (version 16 or newer), open it once to accept the license and
   install the iOS simulator runtime.
2. Clone the repo and open `iosApp/iosApp.xcodeproj`.
3. Pick an iPhone simulator and press ▶ (⌘R). The first build takes a few minutes: the
   *Compile Kotlin Framework* build phase runs Gradle, which downloads JDK 21 and the Kotlin/Native
   toolchain (~1 GB, cached in `~/.gradle` and `~/.konan`).
4. **Running on a real iPhone**: set your Team ID in `iosApp/Configuration/Config.xcconfig`
   (`TEAM_ID = ABCDE12345`). A free Apple ID works for personal devices.

Optional on macOS: Android Studio with the Kotlin Multiplatform plugin can also run the iOS app and
debug Kotlin code running on iOS.

### Troubleshooting

| Symptom | Fix |
|---|---|
| `Sandbox: rsync deny file-write-create` in the Kotlin build phase | `ENABLE_USER_SCRIPT_SANDBOXING` must stay `NO` on the target (already set) |
| `No such module 'Shared'` | Build once (⌘B); the framework is produced by the first build phase |
| Gradle can't find Java from Xcode | Install any JDK (e.g. `brew install --cask temurin`) so `/usr/bin/java` resolves |
| Kotlin changes not picked up | Clean build folder (⇧⌘K) and rebuild |

## Git hooks (both machines)

Install [lefthook](https://lefthook.dev) once, then enable the hooks in your clone:

```bash
# macOS:  brew install lefthook
# Linux:  go install github.com/evilmartians/lefthook/v2@latest
#         (or: npm i -g lefthook, or a binary from github.com/evilmartians/lefthook/releases)
lefthook install
```

| Hook | Runs |
|---|---|
| `commit-msg` | Conventional Commits check (`scripts/git-hooks/check-commit-msg.sh`) |
| `pre-commit` | `ktlintFormat` on staged Kotlin; SwiftLint on staged Swift if installed |
| `pre-push` | detekt, Konsist architecture tests, unit tests |

CI enforces the same rules, so the hooks only give you faster feedback.
