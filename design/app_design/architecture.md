# Current architecture

**Status:** unreleased pre-v0.1.0 learning environment
**Implemented persistence:** PostgreSQL on Amazon RDS in AWS; SQLite for local
development and tests. Aurora is not part of the current implementation.

This document describes what is running now. It supersedes the earlier
milestone-only architecture notes, which are retained in the version history as
historical planning context.

## System context

    Browser
      |
      | HTTPS to one public origin
      v
    CloudFront distribution
      |                    |
      |                    +--> private S3 bucket: React static files
      v
    EC2 public DNS origin on port 8080
      |
      +--> Spring Boot REST API
             |
             +--> private RDS PostgreSQL

The client makes same-origin requests to /api/v1 through CloudFront. CloudFront
uses Origin Access Control for the S3 bucket. Its API origin is the EC2 public
DNS name; the EC2 security group permits port 8080 only from AWS's managed
CloudFront origin-facing prefix list. The database has no public endpoint and
only accepts port 5432 from the API security group. Administrative access uses
Systems Manager rather than an SSH key or inbound SSH rule.

## Application boundaries

    React route/page/component
              |
              v
    HTTP operation client and browser transport
              |
              v
    Spring MVC controller
              |
              v
    Application use case and authorization
              |
              v
    Repository interface
              |
              v
    JDBC or in-memory implementation
              |
              v
    SQLite or PostgreSQL

The REST adapter authenticates an HTTP session, obtains the acting user from
that session, and calls an application use case. The application layer, not the
controller or client, applies financial-context authorization. Repository
interfaces keep application behavior independent of JDBC and let the same
contracts run with in-memory, SQLite, and PostgreSQL implementations.

The Java CLI is a legacy learning adapter. Main starts the REST API, not the
CLI; new browser-facing behavior should be built through the REST boundary.

## Implemented data model

The current persisted model is intentionally narrower than the future
[domain model](domain_model.md). All identities are application-generated UUID
strings. A financial context is both the unit of financial organisation and
the authorization boundary.

    User --< FinancialContextPermission >-- FinancialContext
                                            |
                                            +--< Account
                                            |
                                            +--< Transaction

| Record | Identity | Important fields and rules |
| --- | --- | --- |
| UserRecord | userId | Unique trimmed name and an optional password hash. A hash includes its hashing-strategy identifier; plaintext passwords are never persisted. |
| FinancialContextRecord | financialContextId | Name, optional parent context, and override bit mask. A new context grants its creator OWNER. |
| FinancialContextPermissionRecord | (financialContextId, userId) | READ, WRITE, or OWNER, plus grantor and time. OWNER includes write and read; WRITE includes read. |
| AccountRecord | (financialContextId, accountId) | Name, decimal initial amount, optional parent account, and override bit mask. |
| TransactionRecord | (financialContextId, transactionId) | Origin, target, instant, decimal value, optional parent transaction, and override bit mask. At least one account side must be present. |

PostgreSQL and SQLite each have explicit Flyway migrations. Monetary amounts
are stored as decimal text, and the client uses decimal.js for derived
balances; JavaScript binary floating-point is not used for money calculation.

### Clone semantics

A cloned context is a live scenario branch: it references its parent instead
of eagerly copying every account and transaction. Effective-context resolution
returns child-local records first and then the current unshadowed parent
records. A later parent addition can therefore become visible in an existing
child context.

An inherited account is virtual in the child. Before writing a child
transaction that uses it, the client requests a real child account copy from
the API, preserving the database foreign-key and context boundary.

### Data-model limits

- Currency/unit semantics, tags, recurring rules, projections, assets,
  entities, audit history, reconciliation, and import/export are not yet
  implemented.
- Never edit an applied migration. Create and test a new migration for every
  supported dialect and document recovery before applying it to RDS.
- Context creation plus its initial ownership grant are separate writes. Add an
  application transaction boundary before concurrent or high-value use.

## Security and trust boundaries

- User traffic is HTTPS at CloudFront. Session cookies are HttpOnly, secure in
  AWS, and SameSite=Lax; state-changing requests use CSRF tokens.
- S3 has public-access blocks, bucket-owner-enforced ownership, encryption,
  versioning, Origin Access Control, and an explicit deny for insecure
  transport.
- RDS is private, storage encrypted, and uses an RDS-managed master password in
  Secrets Manager. The API role is the only project role allowed to retrieve
  that secret.
- The GitHub deployment identity has a restricted IAM policy, but it currently
  relies on long-lived access keys in GitHub secrets. This is acceptable only
  as a temporary learning configuration. OIDC is the next identity change.
- CloudFront-to-EC2 traffic is HTTP. The security group makes the origin
  unreachable directly from ordinary internet traffic, but this is not
  end-to-end encryption. A custom domain plus an HTTPS origin (usually an ALB)
  is required before handling sensitive data.
- The CloudFront prefix-list rule proves that a request came from CloudFront's
  network, not specifically this distribution. Add a protected custom origin
  header checked by the API (or an ALB/WAF design) before treating the origin
  as strongly authenticated.

## Delivery and operations

CloudFormation defines two stacks: the regional foundation (VPC, EC2, RDS, S3,
ECR, and roles) and the us-east-1 frontend (CloudFront and S3 policy). The
PowerShell deployment script builds a commit-tagged API image, publishes it to
ECR, deploys it through SSM, builds the client, syncs it to S3, and invalidates
CloudFront.

CI runs for pull requests and development pushes. A main push verifies again
and invokes the production deployment script. Deployments are serialised so a
newer push cannot interrupt a database migration. The deployment currently
replaces the container on one host, so a brief API interruption is expected.

## Known limitations and next architectural decisions

- There is one EC2 instance and one single-AZ RDS instance; the environment is
  not highly available.
- Server-side sessions are stored locally on the API instance. A deployment
  logs users out, and horizontal scaling would need a shared session design or
  another authentication model.
- Multi-record operations such as context creation plus ownership permission
  do not yet have an application transaction boundary.
- The API contract is handwritten in Java and TypeScript. Publish OpenAPI and
  generate client types before the contract grows.
- There is no deployment rollback automation, alerting policy, WAF, origin
  authentication header, or end-to-end browser/API smoke test in CI.
- Do not add Aurora, a load balancer, private application subnets, NAT, or
  multi-AZ capacity merely by default. Record the cost, availability, security,
  and learning reason in an ADR when one becomes necessary.

See [decision_log.md](decision_log.md), [the AWS runbook](../../infra/aws/README.md),
and [the project methodology](../../methodology/methodology.md) for the
associated decisions, operational procedure, and release gates.
