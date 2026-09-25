# Current Implementation

Version `0.1` is a local Java prototype of a financial management
application.

The application currently runs as a Spring Boot process with a REST API. Its
main purpose at this stage is to establish the application's domain,
application, REST, persistence, and authorization boundaries for the React
client in `../client`.

The current implementation supports:

* password-protected user registration and authentication;
* session-authenticated REST endpoints for browser clients;
* financial contexts with shared role-based access;
* lazy financial-context clones and effective-value resolution;
* dated financial-context views, including running account balances and
  totals;
* accounts;
* transactions, including a transfer with one unknown or irrelevant side;
* bootstrap data loaded through application use cases;
* interchangeable repository implementations;
* in-memory, SQLite, and PostgreSQL persistence configurations;
* versioned Flyway schema migrations per SQL dialect; and
* local PostgreSQL repository-contract tests using Testcontainers.

---

## Next Steps

* Extend the React client that consumes the REST API and replaces the legacy
  console UI.
* Add REST endpoints for user management and context-permission grants.
* Add password-hash migration when a current hashing strategy is replaced.
* Extend financial-context views with generated and recurring transactions.
* Add application-level database transaction boundaries for multi-repository
  writes before horizontally scaling the API.

---

## Package Overview

The main source code is under:

```text
src/main/java/com/rgoncalo/financialapp/
```

The project is organized into the following main packages:

```text
com.rgoncalo.financialapp
├── Main.java
├── application
│   ├── account
│   ├── financialcontext
│   ├── security
│   ├── transaction
│   └── user
├── bootstrap
├── cli
│   ├── financialcontext
│   ├── financialcontextview
│   ├── transaction
│   ├── user
│   └── usercontext
├── client
│   └── data
├── commondata
│   ├── account
│   ├── financialcontext
│   ├── money
│   ├── transaction
│   └── user
├── infrastructure
│   ├── persistence
│   │   ├── memory
│   │   └── sqlite
│   └── security
├── logging
├── rest
│   ├── auth
│   └── financialcontext
└── utils
```

### `commondata`

Contains the shared domain data structures used throughout the application.

The main application data is represented with Java `record` classes,
including records and nested ID/value objects. The currently persisted domain
records are:

```text
UserRecord
FinancialContextRecord
FinancialContextPermissionRecord
AccountRecord
TransactionRecord
```

`AccountRecordId` and `TransactionRecordId` both include a
`FinancialContextId`. This makes the context containing an account or
transaction explicit, which is also the authorization boundary for that
record.

`FinancialContextRecord` and `AccountRecord` support lazy cloning. Cloning
persists only the child context and its ownership; no accounts or transactions
are copied. Effective-context resolution returns explicitly stored child
accounts and transactions first, then appends only parent objects that the
child has not explicitly defined. An inherited account retains its source
context ID in the response, making it virtual from the child's perspective.
Before the client writes a transaction using it, the client explicitly creates
a real child account copy and uses its returned child identity in the
transaction request. This makes a clone a live branch: accounts and
transactions subsequently added to a parent are visible to the child without
writing copies into it.

JDBC repositories map these records explicitly to the configured database
dialect's schema.

---

### `application`

Contains application use cases, repository contracts, authorization, and the
application composition facade.

The `Application` class is the main entry point to available use cases. It
creates each use case with the repository implementations supplied through
`ApplicationConfiguration`.

The layer is divided by domain concern:

```text
application
├── account
│   ├──
├── financialcontext
│   ├──
├── transaction
│   ├──
├── user
│   ├──
└── security
    └──
```

The application depends on repository interfaces rather than concrete
persistence implementations. The same use cases can therefore run with
in-memory, PostgreSQL, or SQLite repositories.

Every use case that reads or changes financial data receives an acting
`UserId` in its request. The application layer, rather than the CLI or
repository layer, decides whether that user has permission to perform the
operation.

---

### Users and financial-context access

Users register and log in through the console before a financial-context
session is created. `UserRecord` stores an application identity, name, and a
password hash. It never stores the plaintext password.

Password hashing is behind two application interfaces:

```text
PasswordHashingStrategy
        │ hashes and verifies one algorithm
        ↓
PasswordHashingStrategyResolver
        │ selects the current strategy and resolves stored strategies
        ↓
Pbkdf2PasswordHashingStrategy
```

The current strategy uses PBKDF2-HMAC-SHA-256 with a unique random salt for
each password. Stored hashes include the strategy ID, so a future strategy can
be made current while the registry retains the old strategy to verify existing
users during migration.

Access is granted at the financial-context boundary:

```text
User
  │
  └── FinancialContextPermissionRecord
          │
          └── FinancialContext
                  ├── Account
                  └── Transaction
```

Accounts and transactions do not have independent user ownership. A user may
read or change one of them only when the user has the necessary permission for
the financial context containing it.

Each context membership has one of the following permissions:

```text
READ  → view the context, its accounts, and its transactions, and clone it
WRITE → READ plus create accounts, child account copies, and transactions
OWNER → WRITE plus grant or change other users' context permissions
```

`FinancialContextAuthorization` performs these checks. An `OWNER` permission
also satisfies `WRITE` and `READ`; a `WRITE` permission also satisfies `READ`.
An access failure raises `AccessDeniedException` before the application use
case reads or writes the protected financial data.

Creating a financial context grants its creator `OWNER`. Cloning a context
requires `READ` on the parent and grants `OWNER` on the new child. Changing a
member's role cannot demote the final owner of a context.

Cloned contexts remain linked to their parent data. Resolving the effective
contents of a clone therefore requires `READ` access to the clone and every
context in its parent chain.

---

### `client`

Contains client-side application state and logic.

`ClientApplication` holds the active `UserId`, the currently loaded financial
context, an optional dated financial-context view, and a cache of records
already returned by the application layer.

```text
ClientApplication
        │
        ├── active UserId
        ├── current Financial Context
        ├── optional Financial Context View
        └── client data cache
```

The client supplies its active user ID when it invokes application use cases.
It does not decide permissions itself; the application layer remains the
authority for that decision.

A financial-context view is calculated client-side from the effective
context's accounts and transactions. The view includes transactions through
its selected calendar date (the current date by default), retains each
account's running balances, and exposes each account total at that date.

The legacy client and application layers communicate through direct Java
method calls. The REST layer is the network boundary for future browser
clients and calls the application layer directly.

---

### `cli`

Contains the legacy command-line user interface.

The CLI is organized into nested command groups for user-context operations,
financial-context operations, dated financial-context views, and
transactions. It is responsible for:

* reading commands;
* collecting arguments;
* calling `ClientApplication`; and
* displaying results.

The CLI does not interact directly with repositories or SQLite.

The console remains in the codebase while the REST API is introduced, but
`Main` no longer launches it. The React client in `../client` now provides the
browser interface for the endpoints currently exposed by the API.

The CLI still does not provide commands to grant permissions or switch users
inside an already loaded context.

### Transactions with an unknown or irrelevant account

A transaction may have a `null` origin or target `AccountRecordId`, but not
both. The missing side represents an unknown or irrelevant external account.
This allows, for example, a normal expense to be represented as a transfer
from an account to a `null` target. REST transaction requests may omit either
`originAccountId` or `targetAccountId`.

---

### `rest`

Contains the Spring MVC REST adapter. Controllers receive HTTP requests,
resolve the authenticated session user, call `Application` use cases, and
map their results to HTTP-specific request and response records. They never
read repositories or make authorization decisions directly.

The API uses an HTTP session rather than accepting a `UserId` from the client.
After registration or login, `UserSessionAuthenticator` stores the user's ID
in Spring Security's session context. `CurrentUser` then supplies that ID to
the application use case, where `FinancialContextAuthorization` enforces the
existing context permissions.

All state-changing API requests use Spring Security CSRF protection. The
public `GET /api/v1/auth/csrf` endpoint creates a token and returns its header
name and value. A browser client sends that value in the returned header name
for subsequent `POST` requests and includes its session cookie.

The development CORS configuration permits credentialed requests from
`http://localhost:5173`, the usual Vite development origin. Set
`financial-app.cors.allowed-origin` to the deployed React origin when that
client is hosted separately.

The first REST API version exposes the following endpoints:

```text
GET  /api/v1/auth/csrf
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/auth/me
POST /api/v1/auth/logout

GET  /api/v1/financial-contexts
POST /api/v1/financial-contexts
GET  /api/v1/financial-contexts/{financialContextId}
GET  /api/v1/financial-contexts/{financialContextId}/children
POST /api/v1/financial-contexts/{financialContextId}/clones
GET  /api/v1/financial-contexts/{financialContextId}/accounts
POST /api/v1/financial-contexts/{financialContextId}/accounts
POST /api/v1/financial-contexts/{financialContextId}/accounts/clones
GET  /api/v1/financial-contexts/{financialContextId}/transactions
POST /api/v1/financial-contexts/{financialContextId}/transactions
```

Loading a financial context returns its effective context, accounts, and
transactions. A clone receives child-local objects first, followed by the
current unshadowed objects from its parent. Each account response includes the
context that owns it, allowing the client to recognize virtual inherited
accounts. Before creating a transaction with a virtual account, the client
creates a real child account through the account-clone endpoint and uses that
returned ID. User responses exclude password hashes and all client requests
derive the acting user from the authenticated session.

---

### `spring`

Spring creates and connects the HTTP-layer objects that it manages as beans.
Rather than a controller constructing its own dependencies, it declares them
as constructor parameters and Spring supplies the matching beans when the
application starts.

`Main` is annotated with `@SpringBootApplication`, which enables Spring Boot
configuration, component scanning, and web-server setup. The important
annotations used by this application are:

* `@Configuration` groups explicit bean definitions, such as the security
  configuration.
* `@Bean` marks a factory method whose returned object Spring manages; `Main`
  uses it to create the application facade with its repositories and password
  strategy.
* `@Component` marks a general managed dependency, such as `CurrentUser` and
  `UserSessionAuthenticator`.
* `@RestController` marks a component that receives HTTP requests and returns
  JSON responses.
* `@GetMapping` and `@PostMapping` associate controller methods with API
  routes.
* `@Valid` applies Jakarta validation annotations on a request body before its
  controller method runs.

The default bean scope is one instance per running application. Controllers
therefore do not store per-user mutable state; the authenticated user belongs
to the HTTP session and is resolved separately for each request.

---

### `bootstrap`

Contains the model and runner for optional startup data.

`BootstrapConfigLoader` parses JSON scenario files from `config/bootstrap/`
into a `BootstrapPlan`. Each scenario declares a local user, a mode, and its
ordered commands. `BootstrapStartupRunner` creates or verifies that known user
then delegates to `BootstrapRunner`, which executes the plan through the same
application use cases used by the client. Contexts, accounts, and transactions
therefore follow normal validation and permission rules rather than being
inserted directly with SQL.

---

### `infrastructure`

Contains concrete implementations of external concerns.

Persistence implementations use repository contracts with a JDBC data source
selected by an explicit database dialect:

```text
Repository Interface
        │
        ├── In-Memory Repository
        │
        └── JDBC Repository → PostgreSQL / SQLite
```

The repository contracts currently cover users, financial contexts, context
permissions, accounts, and transactions. This keeps persistence details
outside the application layer.

---

## Application Startup

`Main` is the composition root.

It validates the configured dialect and JDBC URL, creates a data source,
applies that dialect's Flyway migrations, creates the JDBC repositories and
password-hashing strategy registry, and wires them into
`ApplicationConfiguration` and `Application`.

```text
Configured JDBC data source
   ↓
Flyway migrations for PostgreSQL or SQLite
   ↓
Repository implementations
   ↓
Application
   ↓
Spring Boot REST API
   ↓
Authenticated HTTP session
   ↓
Application use cases
```

Users created before password support have no password hash and cannot log in
until their existing user name is registered with a password. Registration
then preserves that user's identity and any context permissions already
assigned to it.

The application layer does not need to know the active SQL dialect. Production
uses `postgresql` with RDS; `sqlite` remains an explicit local option.

Start the API from the `app` directory with:

```text
mvn spring-boot:run
```

Spring Boot listens on port `8080` by default.

---

## Bootstrap Data

Bootstrap data is intended only for a local or otherwise isolated environment.
It is disabled in the default application profile and is never enabled by the
AWS deployment. Startup data requires both the `bootstrap` Spring profile and
`financial-app.bootstrap.enabled=true`; this prevents a normal API start from
changing its database.

The versioned demo scenario is
`config/bootstrap/financial-demo.json`. It creates a known `demo` user with
password `demo-password` and populates the two financial contexts shown in the
browser client. Run it manually from PowerShell with:

```text
$env:SPRING_PROFILES_ACTIVE = "bootstrap"
$env:FINANCIAL_APP_BOOTSTRAP_ENABLED = "true"
$env:FINANCIAL_APP_BOOTSTRAP_CONFIG_FILE = "config/bootstrap/financial-demo.json"
mvn spring-boot:run
```

On Windows, `../scripts/Start-LocalStack.ps1` supplies those settings and opens
the API and Vite terminals. Add `-ResetDatabase` to remove only the local
SQLite database and recreate the exact known demo state. Stop a running API
before using that option.

To add a later scenario, create another JSON file in `config/bootstrap/` with
a `user`, `mode`, and `commands` array, then select it with
`FINANCIAL_APP_BOOTSTRAP_CONFIG_FILE` or the launcher's
`-BootstrapConfigFile` option. The JSON loader rejects unknown fields, making
scenario changes explicit and reviewable.

Commands are executed in file order. The `ref` fields are configuration-local
aliases that let later account and transaction commands refer to generated
financial-context and account IDs.

Supported modes are `never`, `if-empty`, and `always`. `always` creates new
records every time it runs, so use it only for disposable data.

---

## Logging

The application writes structured application output to the console. A
container runtime can collect that output and forward it to its log service.
RDS, rather than the application host, stores durable application data.

---

## Production deployment

The AWS deployment builds an immutable Spring Boot image for the EC2 API host
and builds the React client as static files for a private S3 bucket. CloudFront
serves the client and forwards the same-origin API route to the API host. The
database connection, browser origin, and secure-cookie behavior are supplied
only to the API container at deployment time.

See [`../infra/aws/README.md`](../infra/aws/README.md) for the infrastructure,
release procedure, health checks, and backup requirements.

---

## Request Flow

A normal financial-data request follows this general path:

```text
Authenticated HTTP session
 ↓
REST controller
 ↓
Application use case
 ↓
FinancialContextAuthorization
 ↓
Repository interface
 ↓
Persistence implementation
 ↓
Database
```

For example, account creation follows:

```text
Authenticated HTTP session
 ↓
Account REST controller
 ↓
CreateAccount request with UserId
 ↓
WRITE permission check
 ↓
AccountRepository
 ↓
JDBC / In-Memory Repository
```

Context creation is slightly different because it creates both the context and
its initial ownership:

```text
CreateFinancialContext request with UserId
 ↓
Verify that the user exists
 ↓
FinancialContextRepository.save
 ↓
FinancialContextPermissionRepository.save(OWNER)
```

---

# Persistence

## Repository Abstraction

The application defines repository interfaces for its persisted entities.

Concrete implementations are provided independently from application logic.
This allows the same use cases to run against different persistence systems:

```text
Application
      ↓
Repository Interface
      ↓
┌───────────────────┐
│ In-Memory │ JDBC │
└───────────────────┘
```

The in-memory implementation is useful for lightweight execution and tests,
while the JDBC implementation provides relational persistence for PostgreSQL
and SQLite.

---

## JDBC Schema and Repository Mapping

Flyway migrations create the application's explicit tables and indexes:

```text
users
financial_contexts
financial_context_permissions
accounts
transactions
```

The `financial_context_permissions` table has one row per user and financial
context. It stores the role, the user that granted it, and the grant time. Its
composite primary key prevents duplicate memberships for the same user and
context.

The `users` table stores each user's name, password-hashing strategy ID, and
password hash. The V1 migration represents the existing prototype schema;
later schema changes must be new versioned migrations.

Accounts and transactions use a composite identity of financial-context ID
and record ID. Parent columns retain the links used by lazy clone resolution.

The table and column names are deliberately defined in SQL rather than
generated from Java records. Each dialect has its own migration location, and
SQLite data sources enable foreign keys on every borrowed connection.

Each JDBC application repository owns the SQL for its aggregate and maps each
`ResultSet` row to an application record. The current upsert and query syntax
is shared by PostgreSQL and SQLite; future dialect-specific SQL belongs behind
the explicit dialect boundary.

`JdbcRepository<T>` remains the shared JDBC helper. It handles
statement binding, result-set iteration, resource cleanup, logging, and
persistence exceptions, but it does not derive schema or query information.

```text
JdbcAccountRepository
        ↓ explicit SQL and row mapper
JdbcRepository<AccountRecord>
        ↓ prepared statements and JDBC resource handling
PostgreSQL / SQLite
```

Monetary values and timestamps are converted explicitly in the repository
that persists them. This keeps storage decisions close to the SQL that uses
them.

---

# Testing

Tests are written primarily against the application layer and can run against
multiple repository configurations.

The same test is executed for:

```text
InMemory
SQLite
PostgreSQL
```

This is implemented using JUnit's `@TestTemplate` mechanism and
`RepositoryTestExtension`. The extension provides a
`RepositoryTestConfiguration` parameter to each test invocation.

Conceptually:

```text
One Test
   │
   ├── InMemory configuration
   │       ↓
   │    Execute test
   │
   └── SQLite configuration
           ↓
        Execute same test
   │
   └── PostgreSQL configuration
           ↓
        Execute same test against a disposable local database
```

Each configuration supplies repositories for users, financial contexts,
financial-context permissions, accounts, and transactions.

### In-Memory Tests

Use the in-memory repository implementations.

These provide fast execution without requiring a database connection.

### SQLite Tests

Create an isolated named SQLite in-memory database. It stays alive only for
the test and has foreign-key checks enabled on every connection:

```text
jdbc:sqlite:file:repository-test-...?mode=memory&cache=shared
```

The SQLite Flyway migrations are applied before the repositories are created.
This verifies the same repository contracts and application behavior against
the SQLite dialect.

### PostgreSQL Tests

Testcontainers starts PostgreSQL 16 locally. Each repository-contract test
receives a newly created database, applies the PostgreSQL Flyway migrations,
and removes that database after the test. Docker must be available for this
required production-parity test. In CI it runs directly on the GitHub runner,
not inside a Docker build stage.

The tests currently cover operations such as:

* user creation, lookup, and listing;
* password hashing, successful authentication, and rejected passwords;
* financial-context creation, retrieval, cloning, and inheritance;
* initial context ownership and context-permission grants;
* read/write authorization;
* account creation and listing;
* transaction creation and account-specific transaction queries;
* isolation of accounts and transactions between financial contexts;
* bootstrap execution; and
* client-side financial-context views and account running totals.

This approach helps verify that repository implementations behave
consistently without duplicating the same test for each persistence
implementation.

---

## Current Architectural State

The application currently follows this structure:

```text
REST API
 ↓
Application use cases and authorization
 ↓
Repository interfaces
 ↓
Infrastructure
 ↓
In-Memory / JDBC (PostgreSQL or SQLite)
```

The JDBC implementation has an explicit persistence boundary:

```text
Flyway migrations selected by dialect
      ↓ explicit tables, foreign keys, and indexes
JDBC application repositories
      ↓ explicit SQL and row mapping
JdbcRepository<T>
      ↓ JDBC resource handling
PostgreSQL / SQLite
```

This makes relational constraints, access rules, joins, and future schema
migrations clearer as the data model becomes more sophisticated.

---

## Current Limitations

* The React client covers authentication, contexts, accounts, transactions,
  and context cloning for the first REST API version. It does not yet cover
  forthcoming user-management or context-permission endpoints.
* Authentication uses a server-side HTTP session. Token-based authentication,
  external identity-provider integration, session persistence, and horizontal
  scaling are not implemented.
* The REST API does not yet expose user-management or context-permission
  endpoints.
* Context creation, initial permission creation, and user registration are
  separate persistence operations; they are not yet wrapped in a database
  transaction.
* Currency/unit behavior is not yet implemented.
