# Architecture decision records

Each ADR records the context, decision, consequences, and status of a choice
that will matter after the immediate implementation task. The first four ADRs
were migrated from the original project notes, so their original dates are not
known. Do not retroactively invent them.

## ADR-001: Model transaction classification with tags

Use tags rather than a single category because a transaction can reasonably have
more than one classification. Analysis can later construct categories from root
tags and combinations of tags, such as Food, Fun, and Food and Fun.

The current release has no tag persistence or API. When tags
are implemented, define the query, reporting, inheritance, and rule semantics
before adding a table or UI.

## ADR-002: Technology choice

The initial technology choice was made for convenience and learning velocity,
not from a formal comparative study.

The backend runs on Java/Spring Boot and the client on
React/TypeScript.

Revisit this only when a concrete requirement outweighs the
cost of a migration; do not replace technology merely to follow a trend.

## ADR-003: Use lazy financial-context clones

A financial context and its contents are copied lazily. A child initially
references parent data, and a distinct child record is created only when a
change requires one.

A clone is a live scenario branch, not a snapshot. Effective
context resolution, parent references, override flags, authorization across the
parent chain, and explicit child account copies are required. This behavior
must be visible to users and covered by tests.

## ADR-004: Treat GitHub access keys as temporary and adopt OIDC

The current workflow uses a restricted IAM user's access keys stored as GitHub
Actions secrets because it is the smallest first deployment loop. Replace them
with a GitHub OIDC provider and a repository-and-branch-scoped IAM role before
the system handles sensitive data.

OIDC removes the long-lived AWS secret from GitHub and gives
short-lived, auditable credentials. The trust policy must restrict audience,
repository, and protected branch or environment; it must not trust every
repository in the GitHub account.
