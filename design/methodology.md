In this document we specify the developing process of the app.

# Document rules

Design documents follow the snake_case.

Several different designs or informations may have decoupled versioning. For example, `architecture.md` may have its unique versions for different architectures, named `Architecture version X`. When a design component uses another, its versions should show what are the used versions. For example, in the case of the of the app using architecture: `App version Y (Architecture version X)`.

# Development methodology

We'll follow the DevOps lifecycle:
- Plan
- Code
- Build
- Test
- Release
- Deploy
- Operate
- Monitor

The main component of DevOps is continuous integration and continuous deployment.

## Planning

Planning consists on deciding on new directions and updating the relevant design files, expressing the intent of what is meant to be built.

Planning follows:

```mermaid
flowchart LR
    S[Study] --> M[Model]
    M --> D[Decide]
    D --> P[Planning decision]

    P -. New information or invalid assumption .-> S
    P -. Model needs refinement .-> M
    P -. Alternative needs reconsideration .-> D
```

### Study

Before introducing a new concept, relevant domain knowledge, existing
applications and technical constraints are studied when appropriate.

Examples include:
- studying existing money-management applications;
- studying taxation systems;
- studying financial mathematics;
- studying persistence or architectural alternatives.

### Model

The results of the study are translated into concepts in the domain model.

The model should represent the problem rather than the implementation.

For example, an `Account` is a financial concept and should not initially be
defined in terms of a database table or REST resource.

### Decide

When multiple reasonable alternatives exist, a design decision is made and,
when sufficiently important, recorded in `decision_log.md`.

Decisions should be revisited when new information invalidates their original
assumptions.

## Coding

Coding consists on doing the relevant code and updating the repository.

Implementation proceeds incrementally, prioritizing the smallest useful
vertical slice of functionality.

The implementation should validate the domain model rather than silently
forcing the domain model to fit an arbitrary implementation.

A feature is considered complete only after the relevant parts of the
following have been considered:

- domain model;
- business logic;
- persistence;
- user interaction;
- validation;
- tests.

For example, registering a transaction should not be considered complete
merely because a `Transaction` class exists. The complete behavior includes
validation, updating the financial state and making the transaction
observable to the user.

## Testing

Testing consists on...

# Design principles

## Domain first

The financial domain is modelled independently of the technologies used to
implement it. The domain model describes what information and behavior the
system needs without initially deciding where the information is stored or
how it is exposed.

## Incremental complexity

The application is developed from a simple usable core toward the more
complex final system.

The initial implementation does not need to expose all the capabilities of
the final design. However, decisions made in the initial implementation
should not unnecessarily prevent those capabilities from being introduced
later.

## Generality

The model should describe concepts in sufficiently general terms to support
personal finances, companies and groups of entities.

Specialized behavior should therefore preferably be represented as a
specialization of a general concept rather than by creating unrelated
implementations for each use case.

## Historical and future correctness

Financial information is time-dependent. The design must therefore account
for both the historical state of finances and their projected future state.


# Design layers

The design is developed in several levels of abstraction.

```mermaid
flowchart TD
    DK[Domain knowledge]
    DM[Domain model]
    AD[Application design]
    I[Implementation]

    DK --> DM
    DM --> AD
    AD --> I

    I -. Feedback / discovered constraints .-> AD
    AD -. New domain requirements .-> DM
    DM -. New knowledge required .-> DK
```

## Domain knowledge

Domain knowledge describes concepts and rules that exist independently of
the application.

Examples include:
- interest;
- inflation;
- taxes;
- investments;
- loans.

This knowledge is documented separately from the application implementation.

## Domain model

The domain model describes the concepts that the application needs to
represent.

It defines entities such as:
- accounts;
- transactions;
- financial entities;
- assets;
- contexts;
- units;
- values;
- rules.

The domain model should avoid unnecessary assumptions about databases,
network protocols, user interfaces or programming languages.

## Application design

Application design determines how the domain model is exposed and
implemented.

This includes:
- architecture;
- persistence;
- APIs;
- user interface;
- technology choices;
- deployment.

## Implementation

The implementation is derived from the previous layers and is allowed to
introduce implementation-specific concepts where necessary.

# Coding

Code should follow the conventions of the language and frameworks being used,
unless there is a documented reason to deviate from them.

## Naming

For class names, we use *PascalCase*.

For variable, parameter and function names, we use *camelCase*.

For constants, we use *UPPER_SNAKE_CASE*.

Packages use snake_case.

## Packages and modules

Packages and modules should be organized according to the responsibility
of the code.

Domain concepts should be kept separate from infrastructure and interface
concerns where the architecture requires this separation.

# Version control

## Commits

Each commit should represent a coherent change.

A commit should preferably:
- have a single purpose;
- leave the project in a consistent state;
- include the relevant design changes when the implementation changes the
  documented design.

Commit messages should briefly describe the change using an imperative form.

## Branches

Branches are used to isolate work that is not yet ready to be integrated.

Branch names should describe the work being performed.

Examples:

- `feature/transaction-import`
- `feature/tax-calculation`
- `fix/account-balance`
- `docs/domain-model`

The main branch should contain a coherent and usable state of the project.