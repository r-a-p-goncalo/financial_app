/**
 * Commit messages become part of the project's public history. Conventional
 * Commits makes that history easier to search and can later drive changelogs
 * and semantic-version release notes.
 *
 * Example: feat(client): add transaction filters
 */
module.exports = {
  extends: ["@commitlint/config-conventional"],
  rules: {
    // Keep the vocabulary small without preventing useful project-specific
    // scopes such as api, client, aws, or transactions.
    "type-enum": [
      2,
      "always",
      [
        "build",
        "chore",
        "ci",
        "docs",
        "feat",
        "fix",
        "perf",
        "refactor",
        "revert",
        "style",
        "test"
      ]
    ],
    // This is long enough to be descriptive but short enough for GitHub's
    // commit list and release notes to remain easy to scan.
    "header-max-length": [2, "always", 100]
  }
};
