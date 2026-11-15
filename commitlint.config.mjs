// Conventional Commits, enforced in CI (.github/workflows/commitlint.yml).
// Local equivalent: scripts/git-hooks/check-commit-msg.sh (run by lefthook).
export default {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'header-max-length': [2, 'always', 100],
    'scope-case': [2, 'always', 'kebab-case'],
  },
};
