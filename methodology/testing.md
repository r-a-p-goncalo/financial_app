# Testing

## Goal

Testing provides evidence that the application satisfies its defined behavior
and that changes do not unintentionally break existing behavior. Tests are
part of a feature, not a later cleanup task.

## Test levels

Use the lowest-cost test that provides meaningful confidence:

- unit tests for domain rules, calculations and isolated application behavior;
- integration tests for persistence, security, HTTP boundaries and component
  collaboration;
- end-to-end or smoke tests for the most important user journeys after the
  application is deployed.

Financial calculations, validation rules, authorization and historical data
behavior require explicit examples and boundary cases. A defect should first
be represented by a failing automated test whenever practical, then fixed.

## Quality of tests

Tests must be deterministic, independent and readable. They should explain the
business behavior under test rather than mirror implementation details. Avoid
tests that depend on execution order, a developer's machine, wall-clock time
or external systems unless that dependency is the behavior being verified.

Use realistic test data without real credentials or personal financial data.

## Required checks

Fast local checks run through the pre-commit configuration. Before a pull
request is merged, CI should run at least:

```text
mvn -f app/pom.xml test
npm --prefix client ci
npm --prefix client run lint
npm --prefix client run test
npm --prefix client run build
```

As deployment matures, CI should also run a smoke test against the development
environment. A failing required check blocks integration until it is resolved
or an explicitly documented exception is approved.
