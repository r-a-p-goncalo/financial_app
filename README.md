# Financial App

An educational personal-finance application. The current product lets an
authenticated user create financial contexts (including live scenario clones),
accounts, and dated transactions. It is an **unreleased pre-v0.1.0** project;
it is not yet suitable for real-world financial records.

## What runs today

    React + TypeScript client
            |
            | HTTPS, session cookie and CSRF token
            v
    CloudFront
       |                 |
       |                 +--> private S3 bucket (static client)
       v
    Spring Boot API on one EC2 instance
            |
            v
    Private PostgreSQL RDS instance

The browser uses one CloudFront origin for the client and /api/v1. This keeps
session and CSRF traffic same-origin in production. The application API uses
explicit use cases, authorization checks, repository interfaces, JDBC
implementations, and Flyway migrations. SQLite remains available for local
use; PostgreSQL is the production dialect.

## Start here

- [Current architecture](design/app_design/architecture.md) explains the
  running system, implemented data model, trust boundaries, and known
  limitations.
- [Product definition](design/app_design/product.md) distinguishes delivered
  user journeys from the longer-term
  [domain model](design/app_design/domain_model.md).
- [Backend guide](app/README.md) and [client guide](client/README.md) explain
  code layout and local development.
- [AWS guide](infra/aws/README.md) documents the CloudFormation stacks,
  deployment scripts, operations, and cost safeguards.
- [Development methodology](methodology/methodology.md) documents planning,
  code comments/refactoring, testing, Git, delivery, first-release gates, and
  exactly which documents to update for each change strategy.

## Local development and verification

Prerequisites are Java 21, Maven, Node.js 22 with Corepack/pnpm, Docker, and
PowerShell 7 for AWS scripts. Docker is required for the backend's PostgreSQL
Testcontainers tests.

    # Terminal 1: API (SQLite is the safe local default)
    Set-Location app
    mvn -B -ntp verify
    mvn spring-boot:run

    # Terminal 2: browser client
    Set-Location client
    corepack enable
    pnpm install --frozen-lockfile
    pnpm lint
    pnpm test
    pnpm build
    pnpm dev

The Vite development server proxies /api to http://localhost:8080. Do not put
real financial data, AWS credentials, RDS passwords, or production exports in
the repository.

## Delivery model

Pull requests and pushes to development run CI. A push to main reruns the
verification job and then executes the production deployment script. The
script tags the API image with the full Git commit SHA, runs database migrations
on the EC2 host, publishes the static client to the private bucket, and
invalidates CloudFront.

CI also validates the commits introduced by a pull request (or a push to
development) with Conventional Commits. Use
`type(optional-scope): short description`, for example
`feat(client): add transaction filters` or `docs(aws): explain SSM`. Allowed
types are `build`, `chore`, `ci`, `docs`, `feat`, `fix`, `perf`, `refactor`,
`revert`, `style`, and `test`; headers may be up to 100 characters.

The production workflow currently authenticates with a restricted IAM user's
access keys stored as GitHub secrets. Replace those long-lived keys with GitHub
Actions OIDC before treating this as a production-grade environment. See the
first-release checklist for the other required decisions.

## Documentation rule of thumb

Document a decision, contract, invariant, threat boundary, operational task,
or non-obvious trade-off. Keep code comments close to the code only when they
explain *why* it is shaped this way; names and small methods should explain the
straightforward *what*. The repository intentionally keeps explanatory comments
where they help learning, even when they are more detailed than a production
codebase would normally need.

## License and commercial use

The source code is source-available under the
[PolyForm Noncommercial License 1.0.0](LICENSE). You may use, modify, and
distribute it only for noncommercial purposes under that license. Commercial
use—including a monetised derivative app, subscriptions, advertising, paid
hosting, or paid support based on this code—requires a separate written
commercial license from the copyright holder.

This license governs the repository source code. The Google Play application
will also need its own end-user terms, privacy notice, and subscription/ad
disclosures. Third-party dependencies remain subject to their own licenses.

Before accepting outside code contributions, establish a contributor license
agreement that gives the copyright holder the right to use and relicense
contributions commercially.
