# Current Implementation

Version `0.1` is a local Java prototype of a financial management application.

The application currently runs as a single process and is interacted with through a command-line interface. Its main purpose at this stage is to establish the application's architectural boundaries and persistence model.

The current implementation supports:

* financial contexts;
* accounts;
* transactions;
* interchangeable repository implementations;
* in-memory and SQLite persistence;
* dynamic SQLite schema and query construction based on Java record classes.

---

## Next Steps

* Complete account current-value calculation based on transactions. There is more to be done on accounts and transactions.
* Improve the SQLite schema API and reduce remaining manual configuration while prioritizing future development and performance. We're missing a division of common data representation and database data representation.

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

These records are also used by the SQLite infrastructure to derive database structure dynamically.

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

## Dynamic SQLite Schema

SQLite tables are created dynamically from the Java record classes.

Instead of manually defining each table column as a separate constant, the schema system inspects the structure of a record using reflection.

For example:

```java
public record AccountRecord(
    AccountRecordId accountRecordId,
    String name,
    MonetaryValue initialAmount
) {}
```

Nested records are recursively expanded into columns.

Conceptually:

```text
AccountRecord
├── accountRecordId
│   ├── accountId
│   └── financialContextId
├── name
└── initialAmount
```

becomes a flat SQLite structure similar to:

```text
account_id
account_financial_context_id
name
initial_amount
```

The exact column names are generated from the nested record path.

The process is:

```text
Java Record Class
        ↓
RecordStructure
        ↓
Column Definitions
        ↓
SQLiteTableDefinition
        ↓
CREATE TABLE statement
        ↓
SQLite Table
```

Primary-key columns are currently supplied when the schema is initialized.

This means the record structure defines most of the table automatically, while identity configuration is still explicitly provided.

---

## Dynamic Record Persistence

The SQLite persistence layer uses the same record structure for saving and reading data.

### Saving

`RecordFlattener` recursively converts a record into a flat map of column names and values.

```text
Nested Java Record
        ↓
RecordFlattener
        ↓
column → value
        ↓
Dynamic INSERT query
        ↓
SQLite
```

When saving records, null values can be ignored. This allows partial record objects to be used without generating values for every possible column.

Values are passed through SQLite type converters before being bound to SQL statements.

---

### Reading

When data is retrieved, `RecordConstructor` performs the reverse operation.

```text
SQLite ResultSet
        ↓
Column Values
        ↓
SQLite Type Converters
        ↓
Nested Record Reconstruction
        ↓
Java Record
```

The constructor recursively rebuilds nested record objects using reflection and the record's canonical constructor.

This allows repository code to work with the original record type rather than manually mapping every database column.

---

## Dynamic Queries

The generic `SQLiteRepository<T>` handles common persistence operations.

It dynamically generates SQL for operations such as:

* `INSERT`;
* `SELECT`;
* conditional `SELECT`;
* `DELETE`.

Query conditions can also be constructed from record values.

For example, a nested ID object can be flattened into its corresponding database columns and used to construct the required `WHERE` conditions.

The general flow is:

```text
Record / Partial Record
        ↓
RecordFlattener
        ↓
column/value pairs
        ↓
Query Builder
        ↓
Prepared SQL Statement
```

Prepared statements are used to bind values rather than directly inserting values into SQL strings.

---

## Type Conversion

SQLite does not directly represent every Java type used by the application.

`SQLiteTypeConverters` provides conversions between Java values and database values.

Current converters include support for types such as:

* `String`;
* `Integer`;
* `Double`;
* `Instant`;
* `MonetaryValue`.

Adding support for another persisted value type should generally only require adding the appropriate SQLite type converter rather than modifying each repository.

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

The dynamic SQLite schema is initialized before the repositories are created.

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

The SQLite implementation is largely driven by the Java record model:

```text
Record Classes
      ↓
Dynamic Schema Generation
      ↓
Dynamic Record Flattening
      ↓
Dynamic SQL Generation
      ↓
SQLite
      ↓
Dynamic Record Reconstruction
```

This reduces the amount of entity-specific SQL and mapping code required when the record structure changes.

---

## Current Limitations

* The client and application currently run in the same Java process.
* There is no HTTP/API layer yet.
* SQLite primary-key configuration is still explicitly supplied during schema initialization.
* The schema system does not yet provide database migrations or versioning.
* Currency/unit behaviour is not yet implemented.
* The persistence system currently focuses on the application's record-based data model and may require additional metadata or configuration as database requirements become more complex.
