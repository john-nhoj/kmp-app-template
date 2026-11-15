# Contributing

## Commits: Conventional Commits

```
<type>(<optional scope>)!: <description>

[optional body]

[optional footer(s), e.g. BREAKING CHANGE: ..., Refs: #123]
```

| Type | Use for | Release impact |
|---|---|---|
| `feat` | a new user-facing capability | minor |
| `fix` | a bug fix | patch |
| `perf` | a performance improvement | patch |
| `refactor` | code change without behavior change | — |
| `test` | adding or fixing tests | — |
| `docs` | documentation only | — |
| `build` | Gradle, dependencies, Xcode project | — |
| `ci` | GitHub Actions, fastlane | — |
| `chore` | anything else (tooling, housekeeping) | — |
| `style` | formatting only | — |
| `revert` | reverting a previous commit | — |

`!` after the type/scope (or a `BREAKING CHANGE:` footer) means a breaking change, which bumps the major version.

**Scopes** (optional, kebab-case): a feature (`home`), a platform (`android`, `ios`), or an area
(`shared`, `build`, `deps`). Description: imperative, lowercase start, no trailing period.

```
feat(home): show the device platform in the greeting
fix(ios): cancel the view model when the screen is dismissed
build(deps): update kotlin to 2.4.21
feat(shared)!: require iOS 18
```

## Branches and pull requests

- Branch from `main`: `feat/<short-name>`, `fix/<short-name>`, `chore/<short-name>`.
- Open a PR whose **title is a Conventional Commit**; it becomes the squash-merge commit.
- CI must be green: `./gradlew check` locally covers almost everything CI runs on Linux.
- UI changes: add Android and iOS screenshots to the PR.
