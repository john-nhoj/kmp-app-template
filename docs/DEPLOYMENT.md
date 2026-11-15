# Store deployment

Both pipelines are ready; they only need accounts and secrets. Do Android first, it's quicker.
All secrets go to **Settings → Secrets and variables → Actions → New repository secret**
(or into the `production` environment's secrets).

## Android

**Accounts**: a [Google Play Console](https://play.google.com/console/signup) developer account
(one-time $25 fee, identity verification can take a few days).

### 1. Create the upload key (once, keep it safe forever)

```bash
keytool -genkeypair -v -keystore upload.jks -alias upload \
  -keyalg RSA -keysize 4096 -validity 10000
```

Store `upload.jks` and its passwords in a password manager. With **Play App Signing** (default for
new apps) Google holds the real app-signing key; this upload key can be reset through Play support
if lost, but avoid that.

To sign release builds locally, create an untracked `keystore.properties` at the repo root:

```properties
storeFile=/absolute/path/upload.jks
storePassword=...
keyAlias=upload
keyPassword=...
```

### 2. Create the app in Play Console and upload the first build manually

Google requires the very first upload through the web UI:

1. Play Console → *Create app* (package name = your `app.basePackage`).
2. Build it: `./gradlew :androidApp:bundleRelease` (signed thanks to `keystore.properties`).
3. *Testing → Internal testing → Create new release*, upload
   `androidApp/build/outputs/bundle/release/androidApp-release.aab`, add yourself as a tester.

### 3. Create a service account for CI

1. Google Cloud Console → *IAM & Admin → Service accounts* → create one (no roles needed) → *Keys →
   Add key → JSON*. Download the JSON file.
2. Play Console → *Users and permissions → Invite new users* → the service account email →
   *App permissions*: your app with *Release to testing tracks* (and *Release to production* if you
   later want production uploads).

### 4. Add the secrets and enable

| Secret | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | `base64 -w0 upload.jks` (macOS: `base64 -i upload.jks`) |
| `ANDROID_KEYSTORE_PASSWORD` | keystore password |
| `ANDROID_KEY_ALIAS` | `upload` |
| `ANDROID_KEY_PASSWORD` | key password |
| `PLAY_SERVICE_ACCOUNT_JSON` | full content of the service-account JSON file |

Then set the repository variable `ANDROID_DEPLOY_ENABLED=true` (and `PLAY_RELEASE_STATUS=draft`
until the app has gone through its first review). Test it from **Actions → Deploy Android → Run
workflow**.

## iOS

**Accounts**: [Apple Developer Program](https://developer.apple.com/programs/) ($99/year).

### 1. Register the app

1. [developer.apple.com](https://developer.apple.com/account/resources/identifiers/list) →
   *Identifiers* → **+** → App IDs → App → Bundle ID (explicit) = `PRODUCT_BUNDLE_IDENTIFIER` from
   `Config.xcconfig`.
2. [App Store Connect](https://appstoreconnect.apple.com) → *Apps* → **+** → *New App*, choose that
   bundle ID.
3. Put your **Team ID** (developer.apple.com → Membership) in `Config.xcconfig` → `TEAM_ID`.

### 2. Create an App Store Connect API key

App Store Connect → *Users and Access → Integrations → App Store Connect API* → **+**:

- Access: **Admin** (required for Xcode cloud-managed signing to create the distribution certificate).
- Download the `.p8` file (only possible once) and note the **Key ID** and **Issuer ID**.

### 3. Add the secrets and enable

| Secret | Value |
|---|---|
| `APPLE_TEAM_ID` | your Team ID |
| `ASC_KEY_ID` | API key ID |
| `ASC_ISSUER_ID` | API issuer ID |
| `ASC_KEY_P8_BASE64` | `base64 -i AuthKey_XXXX.p8` |

Then set the repository variable `IOS_DEPLOY_ENABLED=true`. Test it from **Actions → Deploy iOS → Run
workflow**. Builds appear in TestFlight after Apple's processing (10–30 min); internal testers get
them without review.

**How signing works:** the lane passes the API key to `xcodebuild -allowProvisioningUpdates`, so
Xcode creates and downloads the distribution certificate and provisioning profile itself (cloud-managed
signing). No certificates or profiles are stored in the repo or in secrets. If you later need full
control (multiple apps, extensions, enterprise), switch to
[fastlane match](https://docs.fastlane.tools/actions/match/).

## Running the lanes locally

```bash
bundle install                    # once; commit the generated Gemfile.lock
bundle exec fastlane android internal   # needs the same env vars as the workflow
bundle exec fastlane ios beta
```

## Going to production

The pipelines stop at internal testing on purpose. Promote builds from the Play Console
(internal → production) and from App Store Connect (TestFlight build → App Store submission), which
is also where store listings, screenshots and review answers live. Once you're comfortable, you can
add `production` lanes using `upload_to_play_store(track: "production")` and `upload_to_app_store`.
