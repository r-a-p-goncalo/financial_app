# Product definition

## Purpose and current release boundary

Financial App is an educational personal-finance manager. The current
unreleased learning version supports registration and session login, financial
contexts, live scenario clones, accounts, and dated transactions through a
React client and REST API.

It is not an accounting system and must not be used for real-world financial
records. Currency, bank import, reconciliation, recurring rules, tags,
reporting, audit history, user/permission management screens, and regulatory
requirements are outside this release boundary.

The longer-term vision is a financial planner that can model personal, family,
and business finances over time. That vision informs the
[domain model](domain_model.md); it is not a delivery commitment.

## Users

### Individual

An individual wants to record income, expenses, and transfers, then understand
their account balances without needing accounting expertise.

### Household or group

A household may eventually need shared visibility and carefully controlled
changes. The model has context permissions, but the current browser UI does not
yet manage them.

### Accountant or advanced planner

A future advanced user may need categories, reporting, assets, taxes, and
projections. These are deliberately deferred until the simple transaction
workflow is trustworthy.

## Implemented user journeys

### Register a transaction

**Goal:** record a financial movement and see the resulting account balance.

**Preconditions:**

- The user is authenticated.
- The selected financial context is readable and writable by that user.
- At least one account side is known.

**Main flow:**

1. The user opens a financial context and chooses to add a transaction.
2. The user chooses income, expense, or transfer and supplies a neutral decimal
   amount and optional date/time.
3. The client requests an explicit child-account copy when the selected account
   is inherited from a live parent context.
4. The API validates the request and the user's WRITE permission.
5. The API persists the transaction in the selected context.
6. The client invalidates its context query and recalculates running balances
   with decimal-safe rules.

**Important rules:**

- A transaction has an origin, a target, or both; it cannot have neither.
- Origin and target cannot be the same account.
- An account used in a transaction must belong to the selected context.
- A missing side represents an external, unknown, or irrelevant account.
- A transaction date omitted by the client means the save time.

### Create a scenario

A user with READ access can clone a financial context. The clone is live:
unshadowed parent accounts and transactions remain visible until a child change
requires a local record. The cloning user owns the new context.

## Product decisions and planning

- Record lasting technology, architecture, security, or domain decisions in
  [decision_log.md](decision_log.md).
- Record what the implementation actually supports in
  [architecture.md](architecture.md), not in this product vision.
- Record released scope, known limitations, and future roadmap entries in
  [version_history.md](version_history.md).
- Add domain concepts such as tags or units to
  [domain_model.md](domain_model.md) before adding database tables or API
  routes for them.

## Acceptance criteria for new work

A new user-facing slice is ready to integrate when it has a stated user goal,
business rules, validation/error behavior, relevant authorization expectations,
tests at the correct boundaries, and any required update to architecture,
domain model, decision log, or release scope. Avoid creating another planning
Markdown file for a one-off ticket; use the issue and pull request for
short-lived implementation detail.
