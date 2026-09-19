# Documentation usage

## Purpose

Documentation records the application's intent, decisions, constraints and
operational knowledge. It should make it possible to understand why a change
exists without reading every implementation detail.

## Naming and location

Design and methodology document file names use `snake_case`. Documents are
kept near the area they describe:

- `design/domain_knowledge/` records facts and rules that exist independently
  of the application;
- `design/app_design/` records application-specific models and decisions;
- `methodology/` records how the project is planned, built, tested and run.

## Versioned design components

Related design components may evolve independently. When this matters, give a
component an explicit version, such as `Architecture version 2`. A document or
release that depends on it must state the version it uses, for example `App
version 0.3.0 (Architecture version 2)`.

## Updating documentation

Update the relevant document in the same work branch when a change alters:

- a domain concept, rule or assumption;
- an architectural or technology decision;
- an API, persistence or user-facing contract;
- deployment, operating or recovery instructions.

Do not duplicate the same detailed rule in several documents. Link to the
source document instead. Keep an entry in `decision_log.md` for decisions
whose alternatives, consequences or assumptions will matter later.
