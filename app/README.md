# Current Implementation

Version `0.1` is a local Java prototype of a financial management
application.

The application currently runs as a single process and is interacted with
through a command-line interface. Its main purpose at this stage is to
establish the application's domain, application, client, persistence, and
authorization boundaries before a networked client/server deployment exists.

The current implementation supports:

* users without authentication credentials;
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

* Add an authentication boundary that resolves a signed-in principal to a
  `UserId`.
* Add a user-management interface for creating users and granting context
  permissions outside application-code calls.
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
│   └── persistence
│       ├── memory
│       └── sqlite
├── logging
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

Authentication is intentionally not implemented in this prototype.
`UserRecord` contains only an application identity and display name; it has no
password, session, token, or external identity-provider data.

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

The client and application layers currently communicate through direct Java
method calls. There is no network boundary in version `0.1`.

---

### `cli`

Contains the command-line user interface.

The CLI is organized into nested command groups for user-context operations,
financial-context operations, dated financial-context views, and
transactions. It is responsible for:

* reading commands;
* collecting arguments;
* calling `ClientApplication`; and
* displaying results.

The CLI does not interact directly with repositories or SQLite.

The current CLI uses one local development user provisioned at startup. This
keeps the existing single-user workflow usable without adding login behavior.
The user and permission use cases are available through `Application`, but
the CLI does not yet provide commands for user administration or switching
the active user.

### Transactions with an unknown or irrelevant account

A transaction may have a `null` origin or target `AccountRecordId`, but not
both. The missing side represents an unknown or irrelevant external account.
This allows, for example, a normal expense to be represented as a transfer
from an account to a `null` target. In the CLI, leave that account prompt
blank; transaction displays label the missing side as
`Unknown or irrelevant account`.

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
repositories, provisions the local development user, and wires the
repositories into `ApplicationConfiguration` and `Application`.

```text
SQLite connection
   ↓
SQLite schema
   ↓
Repository implementations
   ↓
Local development user and legacy-context permissions
   ↓
Application
   ↓
Optional bootstrap plan
   ↓
ClientApplication
   ↓
CLI
```

When an existing context has no permission records, startup grants the local
development user `OWNER`. This preserves access to data created before the
user and permission model was added. Contexts that already have memberships
are left unchanged.

The application layer does not need to know that SQLite is the current
runtime implementation.

---

## Bootstrap Data

Before opening the CLI, `Main` looks for `config/bootstrap.json` and, when
present, executes its commands through the server-side application use cases.
The supplied file uses `"mode": "if-empty"`, so its sample data is only added
when the local development user cannot access any financial contexts.

Commands are executed in file order. The `ref` fields are configuration-local
aliases that let later account and transaction commands refer to generated
financial-context and account IDs.

Start the app with one of these options when needed:

```text
--bootstrap path/to/bootstrap.json   Use a specific bootstrap file
--no-bootstrap                       Skip all bootstrap data
```

Supported modes are `never`, `if-empty`, and `always`. `always` creates new
records every time the application starts, so use it only for disposable data.

---

## Logging

The application writes logs to `data/financial-app.log`, next to its SQLite
database. The log is cleared when the application starts and is not written to
the console.

---

## Request Flow

A normal financial-data request follows this general path:

```text
Local development user
 ↓
CLI
 ↓
ClientApplication
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
Local development user
 ↓
Financial CLI
 ↓
ClientApplication
 ↓
Current Financial Context
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
while SQLite provides relational persistence for the local CLI application.

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
CLI
 ↓
Client
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

* The client and application run in the same Java process.
* There is no HTTP/API layer yet.
* There is no authentication, login flow, session, token validation, or user
  switching in the CLI.
* User and permission administration are application use cases only; they are
  not yet CLI commands.
* The schema is initialized with `CREATE TABLE IF NOT EXISTS`; it does not yet
  provide versioned migrations.
* Context creation and initial permission creation are separate persistence
  operations; they are not yet wrapped in a database transaction.
* Currency/unit behavior is not yet implemented.
