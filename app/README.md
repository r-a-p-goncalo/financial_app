# Current Implementation

Version `0.1` is a local Java prototype of a financial management application.

The application currently runs as a single process and is interacted with through a command-line interface. Its main purpose at this stage is to establish the application's architectural boundaries and persistence model.

The current implementation supports:

* financial contexts;
* accounts;
* transactions;
* interchangeable repository implementations;
* in-memory and SQLite persistence;
* explicit SQLite schema and repository mappings.

---

## Next Steps

* Complete account current-value calculation based on transactions. There is more to be done on accounts and transactions.
* Introduce versioned database migrations before schema changes need to preserve existing user data.

---

## Package Overview

The main source code is under:

```text
src/main/java/com/rgoncalo/financialapp/
```

The project is organized into the following main packages:

```text
com.rgoncalo.financialapp
├── application
├── cli
├── client
├── commondata
├── infrastructure
└── utils
```

### `commondata`

Contains the shared data structures used throughout the application.

The main application data is represented using Java `record` classes, including records and nested ID/value objects.

SQLite repositories map these records explicitly to the database schema.

---

### `application`

Contains application use cases and repository contracts.

This layer coordinates operations such as:

* creating and retrieving financial contexts;
* creating and listing accounts;
* creating and listing transactions;
* enforcing isolation between financial contexts.

The application depends on repository interfaces rather than concrete persistence implementations.

The `Application` class acts as the main entry point to the available use cases.

---

### `client`

Contains client-side application state and logic.

The client maintains session-level state, particularly the currently loaded financial context.

```text
User loads Financial Context A
        ↓
ClientApplication stores Context A
        ↓
Subsequent operations use Context A automatically
```

The client and application layers currently communicate through direct Java method calls. There is no network boundary in version `0.1`.

---

### `cli`

Contains the command-line user interface.

The CLI is responsible for:

* reading commands;
* collecting arguments;
* calling `ClientApplication`;
* displaying results.

The CLI does not interact directly with repositories or SQLite.

---

### `infrastructure`

Contains concrete implementations of external concerns.

Persistence implementations currently include:

```text
Repository Interface
        │
        ├── In-Memory Repository
        │
        └── SQLite Repository
```

This keeps persistence details outside the application layer.

---

## Application Startup

`Main` acts as the composition root.

It selects and connects the concrete runtime dependencies:

```text
SQLite
   ↓
Repository implementations
   ↓
Application
   ↓
ClientApplication
   ↓
CLI
```

The application layer therefore does not need to know which persistence implementation is being used.

## Bootstrap data

Before opening the CLI, `Main` looks for `config/bootstrap.json` and, when
present, executes its commands through the server-side application use cases.
The supplied file uses `"mode": "if-empty"`, so its sample data is only added
to a new database.

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

## Request Flow

A normal request follows this general path:

```text
User
 ↓
CLI
 ↓
ClientApplication
 ↓
Application Use Case
 ↓
Repository Interface
 ↓
Persistence Implementation
 ↓
Database
```

For example, account creation follows:

```text
User
 ↓
Financial CLI
 ↓
ClientApplication
 ↓
Current Financial Context
 ↓
Create Account
 ↓
AccountRepository
 ↓
SQLite / In-Memory Repository
```

---

# Persistence

## Repository Abstraction

The application defines repository interfaces for its main persisted entities.

Concrete implementations are provided independently from the application logic.

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

The in-memory implementation is useful for lightweight execution and testing, while SQLite provides relational persistence.

---

## SQLite Schema and Repository Mapping

`SQLiteSchema` creates the application's explicit tables and indexes:

```text
financial_contexts
accounts
transactions
```

The table and column names are deliberately defined in SQL rather than
generated from Java records. This keeps the database contract stable when
application models evolve and makes future migrations straightforward.

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

Tests are written against the application layer and can run against multiple repository configurations.

The same test is executed for:

```text
InMemory
SQLite
```

This is implemented using JUnit's `@TestTemplate` mechanism and `RepositoryTestExtension`.

The extension provides a `RepositoryTestConfiguration` parameter to each test invocation.

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

Each configuration creates the repository implementations required by the test.

### In-Memory Tests

Use the in-memory repository implementations.

These provide fast execution without requiring a database connection.

### SQLite Tests

Create an isolated SQLite in-memory database:

```text
jdbc:sqlite::memory:
```

The explicit SQLite schema is initialized before the repositories are created.

This means the same application behaviour can be tested against both:

```text
Repository Contract
        ↑
        │
Application Test
        │
        ├── In-Memory
        └── SQLite
```

The tests currently cover operations such as:

* financial-context creation and retrieval;
* account creation and listing;
* transaction creation;
* isolation of accounts between financial contexts;
* isolation of transactions between financial contexts.

This approach helps verify that repository implementations behave consistently without duplicating the same test for each persistence implementation.

---

## Current Architectural State

The application currently follows this structure:

```text
CLI
 ↓
Client
 ↓
Application
 ↓
Repository Interfaces
 ↓
Infrastructure
 ↓
In-Memory / SQLite
```

The SQLite implementation has an explicit persistence boundary:

```text
SQLiteSchema
      ↓ explicit tables and indexes
SQLite application repositories
      ↓ explicit SQL and row mapping
SQLiteRepository<T>
      ↓ JDBC resource handling
SQLite
```

This makes relational constraints, indexes, joins, and future schema migrations
clearer as the data model becomes more sophisticated.

---

## Current Limitations

* The client and application currently run in the same Java process.
* There is no HTTP/API layer yet.
* The schema is initialized with `CREATE TABLE IF NOT EXISTS`; it does not yet provide database migrations or versioning.
* Currency/unit behaviour is not yet implemented.
* Foreign-key constraints and database-level validation will be added as the domain rules become more complete.
