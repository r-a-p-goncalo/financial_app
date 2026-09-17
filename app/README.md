# Current Implementation

Version `0.1` is a local Java prototype of a financial management
application.

The application currently runs as a Spring Boot process with a REST API. Its
main purpose at this stage is to establish the application's domain,
application, REST, persistence, and authorization boundaries before a React
client is introduced.

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
* in-memory and SQLite persistence; and
* explicit SQLite schema and repository mappings.

---

## Next Steps

* Create the React client that consumes the REST API and replaces the legacy
  console UI.
* Add REST endpoints for user management and context-permission grants.
* Add password-hash migration when a current hashing strategy is replaced.
* Extend financial-context views with generated and recurring transactions.
* Introduce versioned database migrations before schema changes need to
  preserve deployed user data.

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

`FinancialContextRecord`, `AccountRecord`, and `TransactionRecord` support
lazy cloning. A cloned record retains a link to its parent record and a bit
mask describing which attributes the child overrides. Effective values are
resolved without altering the stored rows.

SQLite repositories map these records explicitly to the database schema.

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
in-memory or SQLite repositories.

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
READ  → view the context, its accounts, and its transactions
WRITE → READ plus create accounts and transactions, and clone the context
OWNER → WRITE plus grant or change other users' context permissions
```

`FinancialContextAuthorization` performs these checks. An `OWNER` permission
also satisfies `WRITE` and `READ`; a `WRITE` permission also satisfies `READ`.
An access failure raises `AccessDeniedException` before the application use
case reads or writes the protected financial data.

Creating a financial context grants its creator `OWNER`. Cloning a context
requires `WRITE` on the parent and grants `OWNER` on the new child. Changing a
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
`Main` no longer launches it. A React client will replace it.

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
GET  /api/v1/financial-contexts/{financialContextId}/transactions
POST /api/v1/financial-contexts/{financialContextId}/transactions
```

Loading a financial context returns its effective context, accounts, and
transactions, including values inherited from a parent clone. User responses
exclude password hashes and all client requests derive the acting user from
the authenticated session.

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

`BootstrapConfigLoader` parses `config/bootstrap.json` into a `BootstrapPlan`.
`BootstrapRunner` executes that plan through the same application use cases
used by the client. The runner receives the active user ID, so created
contexts, accounts, and transactions follow the same permission rules as
normal operations.

---

### `infrastructure`

Contains concrete implementations of external concerns.

Persistence implementations currently include one implementation per
repository contract:

```text
Repository Interface
        │
        ├── In-Memory Repository
        │
        └── SQLite Repository
```

The repository contracts currently cover users, financial contexts, context
permissions, accounts, and transactions. This keeps persistence details
outside the application layer.

---

## Application Startup

`Main` is the composition root.

It creates the SQLite connection, initializes the schema, creates the SQLite
repositories and password-hashing strategy registry, and wires them into
`ApplicationConfiguration` and `Application`.

```text
SQLite connection
   ↓
SQLite schema
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

The application layer does not need to know that SQLite is the current
runtime implementation.

Start the API from the `app` directory with:

```text
mvn spring-boot:run
```

Spring Boot listens on port `8080` by default.

---

## Bootstrap Data

`BootstrapConfigLoader` and `BootstrapRunner` remain available for creating
sample data through application use cases. `Main` does not currently invoke a
bootstrap plan when starting the REST API; a future protected administrative
endpoint or development profile can decide when and for whom to run one.

Commands are executed in file order. The `ref` fields are configuration-local
aliases that let later account and transaction commands refer to generated
financial-context and account IDs.

Supported modes are `never`, `if-empty`, and `always`. `always` creates new
records every time it runs, so use it only for disposable data.

---

## Logging

The application writes logs to `data/financial-app.log`, next to its SQLite
database. The log is cleared when the application starts and is not written to
the console.

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
SQLite / In-Memory Repository
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
│ In-Memory │ SQLite │
└───────────────────┘
```

The in-memory implementation is useful for lightweight execution and tests,
while SQLite provides relational persistence for the local API application.

---

## SQLite Schema and Repository Mapping

`SQLiteSchema` creates the application's explicit tables and indexes:

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
password hash. The schema initialization adds the two password columns when
opening a database created before password support.

Accounts and transactions use a composite identity of financial-context ID
and record ID. Parent columns retain the links used by lazy clone resolution.

The table and column names are deliberately defined in SQL rather than
generated from Java records. `SQLiteConnection` enables SQLite foreign keys
for the application connection, and `SQLiteSchema` defines the relevant
relationships and indexes explicitly.

Each SQLite application repository owns the SQL for its aggregate and maps
each `ResultSet` row to an application record. For example,
`SQLiteAccountRepository` defines its account `INSERT` and `SELECT`
statements alongside the code that builds an `AccountRecord`.

`SQLiteRepository<T>` remains the shared JDBC helper. It handles prepared
statement binding, result-set iteration, resource cleanup, logging, and
persistence exceptions, but it does not derive schema or query information.

```text
SQLiteAccountRepository
        ↓ explicit SQL and row mapper
SQLiteRepository<AccountRecord>
        ↓ prepared statements and JDBC resource handling
SQLite
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
```

Each configuration supplies repositories for users, financial contexts,
financial-context permissions, accounts, and transactions.

### In-Memory Tests

Use the in-memory repository implementations.

These provide fast execution without requiring a database connection.

### SQLite Tests

Create an isolated SQLite in-memory database:

```text
jdbc:sqlite::memory:
```

The explicit SQLite schema is initialized before the repositories are
created. This verifies the same repository contracts and application behavior
against SQLite mappings.

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
In-Memory / SQLite
```

The SQLite implementation has an explicit persistence boundary:

```text
SQLiteSchema
      ↓ explicit tables, foreign keys, and indexes
SQLite application repositories
      ↓ explicit SQL and row mapping
SQLiteRepository<T>
      ↓ JDBC resource handling
SQLite
```

This makes relational constraints, access rules, joins, and future schema
migrations clearer as the data model becomes more sophisticated.

---

## Current Limitations

* The React client has not been created yet; the legacy Java client remains
  in-process code and is not launched by `Main`.
* Authentication uses a server-side HTTP session. Token-based authentication,
  external identity-provider integration, session persistence, and horizontal
  scaling are not implemented.
* The REST API does not yet expose user-management or context-permission
  endpoints.
* The schema is initialized with `CREATE TABLE IF NOT EXISTS`; it does not yet
  provide versioned migrations.
* Context creation, initial permission creation, and user registration are
  separate persistence operations; they are not yet wrapped in a database
  transaction.
* Currency/unit behavior is not yet implemented.
