# Build

## Goal

A build turns a specific source revision into repeatable, deployable
artifacts. The same revision and declared dependency versions should produce
the same application behavior regardless of the developer or CI runner.

## Build inputs and outputs

The backend build uses Maven from `app/`; the frontend build uses the Node
dependency lockfile and scripts from `client/`. Build outputs, dependencies,
local databases, logs and environment files are not source artifacts and must
not be committed.

CI installs dependencies from the lockfiles, performs a clean build, and
publishes or deploys the resulting artifact only after its checks pass. A
deployment must promote that built artifact instead of rebuilding source for
a later environment.

## Build discipline

- Keep build commands documented and runnable from a clean checkout.
- Pin or lock dependencies where the ecosystem supports it.
- Fail on compilation, type-checking, linting or packaging errors.
- Give artifacts a traceable version, such as the Git commit SHA and release
  tag.
- Never put credentials or environment-specific production configuration in a
  build artifact.

Build failures are treated as defects in the current integration state and are
fixed before unrelated work is merged.
