# Release

## Release scope

A release is a product milestone, not a permanent development branch. Its
scope is chosen from work already integrated into `development` and should be
documented with the user-visible changes, known limitations and required
deployment steps.

## Release branch

When the chosen scope is complete, create `release/<major>.<minor>.<patch>`
from `development`, for example `release/0.1.0`. A release branch accepts only
release-blocking fixes, documentation corrections, final tests, compatibility
or packaging changes. New functionality belongs in a work branch from
`development` for a later release.

## Release gates

Before release, required CI checks, security-sensitive checks, manual
acceptance checks and deployment smoke tests must pass. Confirm that the
release notes, migration steps, rollback method and environment configuration
are ready.

Merge the approved release branch into `main` and create an annotated release
tag in the form `v<major>.<minor>.<patch>`, such as `v0.1.0`. The tag identifies
the exact released commit and is immutable after publication.

Release branches are temporary and are deleted after the release is complete.

## Hotfixes

For a supported released version, start `fix/<short-description>` from the
applicable release tag or release branch. Merge the fix to `main`, create the
appropriate patch tag, and also merge or cherry-pick the fix into
`development` when it remains applicable. This prevents a production fix from
being lost in the next version.
