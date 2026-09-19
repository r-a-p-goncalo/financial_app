# Development methodology

This document is the concise overview of how the application is developed.
The linked documents contain the rules and procedures for each practice.

## Documentation

Documentation records the intent, decisions and constraints that guide the
application. Design document names use `snake_case`, and related documents can
have independent versions when their relationship is explicit. See
[documentation usage](documentation_usage.md).

## Lifecycle

The application follows a DevOps lifecycle. Work moves through the following
activities, with feedback allowed at every stage:

1. [Plan](planning.md): study the problem, model it and record decisions.
2. [Code](coding.md): implement the smallest useful vertical slice.
3. [Build](build.md): produce reproducible application artifacts.
4. [Test](testing.md): verify behavior at the appropriate level.
5. [Release](release.md): prepare, approve, tag and document a version.
6. Deploy: promote the same verified artifact through
   environments.
7. Operate: run the application safely and respond to issues.
8. Monitor: observe health, behavior and cost.

Continuous integration verifies every proposed change. Continuous deployment
automates delivery to development environments and uses explicit production
release controls until automatic production deployment is justified.

## Design approach

The design is domain-first, incrementally complex, sufficiently general for
the financial contexts the application supports, and correct for historical
and future financial information. Domain knowledge informs the domain model;
the model informs application design; and application design informs the
implementation. See [design methodology](design.md).

## Version control

Git represents temporary units of work, while design documents represent the
application's structure and decisions. Use short-lived work branches, keep
`development` buildable, release stable versions from `main`, and tag every
release. See [Git usage](git_usage.md).
