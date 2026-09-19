# Git usage

## Goals

Git records coherent, reviewable changes and provides a reliable path from
planned work to a tagged release. Branches are temporary units of development
work; they do not replace the domain or application design documents.

## Branch model

The repository has two long-lived branches:

- `main` contains the latest released, stable version of the application.
- `development` contains the integrated, buildable state of the next version.

All new work starts in a short-lived branch from an up-to-date `development`
branch and returns to `development` through a pull request. Use a branch name
that describes the work, not the version in which it may ship:

```text
feature/transaction-import
fix/account-balance
refactor/transaction-model
docs/domain-model
chore/update-tooling
```

A large feature is divided into several branches only when its parts can be
integrated independently. Do not create a branch merely to represent a design
layer or future release version.

## Commit strategy

Each commit has one coherent purpose, leaves the project in a consistent state
and includes the relevant design update when it changes a documented design.
Avoid mixing feature work, formatting churn, generated files and unrelated
cleanup in one commit.

Use concise Conventional Commit messages:

```text
feat(client): show account balance beside transactions
fix(api): reject transactions with invalid account IDs
test(persistence): cover transaction rollback
docs: describe the release procedure
refactor(domain): simplify monetary value comparison
chore: update gitignore
```

Use the imperative form after the type. A commit body explains motivation,
constraints or migration impact when the summary alone is not enough.

Generated build outputs, dependencies, local databases, logs, `.env` files,
credentials and secrets are not committed. If a generated file was previously
tracked, remove it from Git tracking while preserving the local file before
relying on `.gitignore`.

## Local checks

The repository uses `pre-commit` to run fast checks before Git creates a
commit. A pre-commit hook is a local script registered in `.git/hooks/` that
Git runs automatically after files are staged and before it records them. A
hook can reject the commit when a check fails, preventing common mistakes from
entering a branch.

The hook definitions are versioned in `.pre-commit-config.yaml`. Each developer
installs the hook once after cloning the repository or changing the hook
configuration:

```text
python -m pre_commit install --install-hooks
```

The current hooks:

- remove trailing whitespace;
- ensure text files end with a newline;
- validate YAML syntax;
- reject newly added files that are unexpectedly large;
- detect private keys; and
- run the client ESLint check when files in `client/` change.

The whitespace and end-of-file hooks fix files automatically, then fail the
first run so that the developer can review and stage the resulting changes.
This is expected behavior, not a broken hook. After reviewing the diff, stage
the intended changes and commit again.

Run all configured hooks explicitly after changing the hook configuration or
when validating the whole repository:

```text
python -m pre_commit run --all-files
```

Hooks must remain fast enough for normal commits. They catch formatting,
accidental secrets and quick static checks; they do not replace the complete
test and build suite. Java tests, frontend tests and production builds remain
CI requirements because they take longer and must run in a clean, shared
environment.

Do not routinely bypass a hook with `--no-verify`. If an emergency requires it,
run the skipped check and record the follow-up before merging.

## Pull requests and integration

Open a pull request when a branch is ready to be integrated. Its description
states the behavior changed, design decisions or migrations, tests run and
remaining risks. Keep pull requests small enough to review meaningfully.

The target branches must be protected as the project matures:

- disallow direct pushes and force pushes to `main`;
- require passing CI checks before merging to `development` and `main`;
- require an approving review when a second contributor is available;
- use the pull request as the audit trail even when working alone.

Use squash merge by default so each merged work branch appears as one clear
integration change. Preserve the branch commits only when each is independently
useful and readable. After the merge, delete both the remote and local work
branch. Never rewrite published `main` or `development` history; rebase only a
private work branch when needed to update it before review.

## Release branches and tags

When the scope of a version is integrated into `development`, create a
temporary `release/<major>.<minor>.<patch>` branch. It accepts only
release-blocking fixes, release documentation, compatibility, packaging and
final test work. It must not receive new functionality for later versions.

After the release checks pass, merge the release branch into `main` and create
an annotated tag in this form:

```text
v<major>.<minor>.<patch>
```

Examples are `v0.1.0`, `v0.1.1` and `v1.0.0`. A tag identifies the exact
released commit. Release branches are deleted after the tag is published. The
full release procedure is in [release.md](release.md).

## Fixes for released versions

When a supported release needs a fix, create `fix/<short-description>` from
the relevant release tag or release branch. Integrate it into `main`, issue an
appropriate patch release, and merge or cherry-pick it into `development` when
the correction remains applicable. This prevents production fixes from being
lost in the next version.
