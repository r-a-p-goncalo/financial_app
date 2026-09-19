# Coding

## Implementation strategy

Implement the smallest useful vertical slice rather than completing one
technical layer in isolation. A feature is complete only after considering the
relevant domain behavior, business logic, persistence, user interaction,
validation and tests.

For example, registering a transaction is not complete merely because a
`Transaction` class exists. The behavior must validate the request, update the
financial state and make the result observable to the user.

Implementation should validate the domain model. Do not silently reshape the
model around an arbitrary implementation convenience; update the design and
decision record if a discovered constraint requires the model to change.
