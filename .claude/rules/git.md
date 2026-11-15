# Git branching

Applies to every change. `CONTRIBUTING.md` is the human-facing version of these rules.

## Branches
- **Never commit on `main`.** Every change reaches `main` through a pull request.
- **Name**: `<type>/<issue>-<slug>`, where `<type>` is the Conventional Commit type of the work
  (`feat`, `fix`, `chore`, `docs`, `refactor`, `build`, `ci`, `test`, `perf`) and `<slug>` is a few
  kebab-case words from the issue title: `feat/3-logout`, `fix/12-crash-on-sign-out`,
  `chore/8-change-domain`. Without an issue: `<type>/<slug>` (`chore/git-branching-rule`).
- **One issue or concern per branch.** Something unrelated that comes up mid-task gets its own
  branch and PR, not an extra commit on the current one.
- **Base**: `origin/main`, after `git fetch origin`. Base on an open PR's branch only when the work
  needs code that exists only there; then target the PR at that branch and say it is stacked on #<pr>.

## Worktrees
- Each branch is built in its own git worktree, not in the main checkout (the `new-feature` skill
  covers the details). In Herdr, `herdr worktree create` picks the location; otherwise use
  `../<repo>-worktrees/<branch with / → ->` next to the repo.
- **Never nest a worktree inside the repo**: Konsist scans every `.kt` file under the project dir.
- Gitignored files (`local.properties`) are not copied into a new worktree.
- The stash is shared by all worktrees: prefer a WIP commit over `git stash`.

## Commits and history
- Conventional Commits (see `CONTRIBUTING.md`), with a `Refs: #<n>` footer when there is an issue.
- Amend or reorder only commits that are not pushed yet. Never force-push, rebase pushed commits or
  delete a remote branch without asking.

## Pull requests
- Push and open a PR only when asked. Title: a Conventional Commit. Body:
  `.github/pull_request_template.md` filled in (it becomes the squash commit body): `Closes #<n>`,
  and under "Breaking changes" either `None.` or a `BREAKING CHANGE: ...` line. Assign it to the
  user (`--assignee @me`); add `--base <branch>` when stacked.
- **Merge with "Squash and merge" only** (`gh pr merge --squash`), never a merge commit or rebase.
  The PR title becomes the single commit on `main`, and release-please reads it for the version
  bump and CHANGELOG, so keep the title accurate when the scope changes. Merge only when asked.
- After a stacked PR's base is squash-merged, its branch still carries the base's original commits:
  retarget the PR to `main` and `git rebase --onto origin/main <old base branch>`. That rewrites
  pushed commits, so ask before force-pushing it (`--force-with-lease`).
- Leave branches and worktrees in place after merging; remove them only when asked.
