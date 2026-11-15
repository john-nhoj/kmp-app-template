#!/usr/bin/env bash
# Turns this template into your app: package / bundle id, app name, class names, directories.
#
#   ./scripts/rename.sh com.acme.notes "Acme Notes"
#
# Run it once, right after creating a repository from the template, then review `git diff`.
set -euo pipefail

OLD_PACKAGE="com.example.template"
OLD_NAME="Template"

if [[ $# -ne 2 ]]; then
  echo "Usage: $0 <package.id> \"<App Name>\"   e.g. $0 com.acme.notes \"Acme Notes\"" >&2
  exit 1
fi

NEW_PACKAGE="$1"
NEW_DISPLAY_NAME="$2"
# Identifier-safe name for classes/types: "Acme Notes" -> "AcmeNotes".
NEW_NAME="$(printf '%s' "$NEW_DISPLAY_NAME" | perl -pe 's/[^A-Za-z0-9]+(.)?/\U$1/g; s/^(.)/\U$1/')"

if ! [[ "$NEW_PACKAGE" =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$ ]]; then
  echo "✖ '$NEW_PACKAGE' is not a valid package id (lowercase, dot-separated, e.g. com.acme.notes)" >&2
  exit 1
fi
if ! [[ "$NEW_NAME" =~ ^[A-Z][A-Za-z0-9]*$ ]]; then
  echo "✖ Could not derive a valid type name from '$NEW_DISPLAY_NAME'" >&2
  exit 1
fi

cd "$(dirname "$0")/.."
echo "→ $OLD_PACKAGE → $NEW_PACKAGE"
echo "→ $OLD_NAME → $NEW_NAME (display name: \"$NEW_DISPLAY_NAME\")"

# Text files tracked by the project (skips build outputs, VCS data and binaries).
text_files() {
  find . \( -path ./.git -o -path ./.gradle -o -path ./.kotlin -o -name build -o -name node_modules \
    -o -path ./.idea -o -name xcuserdata \) -prune -o -type f \
    \( -name '*.kt' -o -name '*.kts' -o -name '*.swift' -o -name '*.xml' -o -name '*.properties' \
    -o -name '*.pbxproj' -o -name '*.xcscheme' -o -name '*.xcconfig' -o -name '*.yml' -o -name '*.json' \
    -o -name '*.md' -o -name 'Appfile' -o -name 'Fastfile' -o -name '*.toml' -o -name '*.pro' \) -print0
}

# 1. Package / bundle id everywhere (code, Gradle, Xcode, fastlane, docs).
text_files | xargs -0 perl -pi -e "s/\Q$OLD_PACKAGE\E/$NEW_PACKAGE/g"

# 2. Move Kotlin source directories: .../kotlin/com/example/template -> .../kotlin/<new/path>.
OLD_PATH="${OLD_PACKAGE//.//}"
NEW_PATH="${NEW_PACKAGE//.//}"
# Collect first: moving directories while `find` is still walking the tree confuses it.
package_dirs="$(find . \( -path ./.git -o -path ./.gradle -o -name build \) -prune -o -type d -path "*/kotlin/$OLD_PATH" -print)"
printf '%s\n' "$package_dirs" | while read -r dir; do
  [[ -n "$dir" ]] || continue
  root="${dir%/"$OLD_PATH"}"
  # Go through a temp dir so the new path may safely live inside the old one (com.example.template.app).
  tmp="$(mktemp -d "$root/.rename.XXXXXX")"
  find "$dir" -mindepth 1 -maxdepth 1 -exec mv {} "$tmp/" \;
  find "$root" -mindepth 1 -type d -empty -delete
  mkdir -p "$root/$NEW_PATH"
  find "$tmp" -mindepth 1 -maxdepth 1 -exec mv {} "$root/$NEW_PATH/" \;
  rmdir "$tmp"
done

# 3. Type names, theme/style names, product names (code & project files only, not docs).
code_files() {
  find . \( -path ./.git -o -path ./.gradle -o -name build -o -path ./docs -o -path ./.claude \) -prune -o -type f \
    \( -name '*.kt' -o -name '*.kts' -o -name '*.swift' -o -name '*.xml' -o -name '*.pbxproj' \
    -o -name '*.xcscheme' \) -print0
}
code_files | xargs -0 perl -pi -e "
  s/\b${OLD_NAME}(Application|Theme|App)\b/${NEW_NAME}\$1/g;
  s/Theme\.${OLD_NAME}\b/Theme.${NEW_NAME}/g;
  s/\b${OLD_NAME}\.app\b/${NEW_NAME}.app/g;
"
perl -pi -e "s/^rootProject\.name = \"${OLD_NAME}\"/rootProject.name = \"${NEW_NAME}\"/" settings.gradle.kts
perl -pi -e "s|<string name=\"app_name\">.*</string>|<string name=\"app_name\">${NEW_DISPLAY_NAME}</string>|" \
  androidApp/src/main/res/values/strings.xml
perl -pi -e "s/^APP_DISPLAY_NAME = .*/APP_DISPLAY_NAME = ${NEW_DISPLAY_NAME}/; s/^PRODUCT_NAME = .*/PRODUCT_NAME = ${NEW_NAME}/" \
  iosApp/Configuration/Config.xcconfig

# 4. Rename files named after the app.
find . \( -path ./.git -o -name build \) -prune -o -type f \( -name "${OLD_NAME}Application.kt" -o -name "${OLD_NAME}App.swift" \) -print |
  while read -r file; do mv "$file" "${file/$OLD_NAME/$NEW_NAME}"; done

echo "✔ Done. Next steps:"
echo "  1. Review the changes:       git diff --stat"
echo "  2. Verify the build:         ./gradlew ktlintFormat :androidApp:assembleDebug testAndroidHostTest :konsist:test"
echo "  3. Set your Apple Team ID:   iosApp/Configuration/Config.xcconfig (TEAM_ID)"
echo "  4. Commit:                   git commit -am \"chore: rename template to $NEW_DISPLAY_NAME\""
