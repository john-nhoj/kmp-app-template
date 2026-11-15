#!/usr/bin/env sh
# Validates a commit message against Conventional Commits (https://www.conventionalcommits.org).
# Mirrors commitlint.config.mjs, which CI enforces. Usage: check-commit-msg.sh <path-to-message-file>
set -eu

header=$(head -n1 "$1")

# Let git's own generated messages through.
case "$header" in
  Merge\ *|Revert\ *|fixup!\ *|squash!\ *|amend!\ *) exit 0 ;;
esac

types="build|chore|ci|docs|feat|fix|perf|refactor|revert|style|test"
pattern="^($types)(\([a-z0-9._/-]+\))?!?: [^A-Z].{0,98}[^.]$"

if ! printf '%s' "$header" | grep -Eq "$pattern"; then
  cat >&2 <<MSG
✖ Commit message does not follow Conventional Commits:

    $header

Expected:  <type>(<optional scope>)!: <description>
  types:   $(printf "%s" "$types" | sed "s/|/, /g")
  e.g.     feat(home): show platform greeting
           fix(android): handle missing platform name
           feat(api)!: drop v1 endpoints   (! = breaking change)

Description: starts lowercase, no trailing period, header <= 100 chars.
MSG
  exit 1
fi
