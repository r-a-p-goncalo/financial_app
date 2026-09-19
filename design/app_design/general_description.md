# Finances manager


This app is meant to serve as manager of personal finances, manager of company finances, manager of multiple different entity finances. It is also meant to work as a planner, creating projections for the future using the current (real) state as a basis, to know, with defined transactions and rules and so on, how much money each entity will have in each account for each future day.

The initial design of the app is meant to be locally hosted, used by a single user (not a company, nor a group), even if it is meant to be later extended to being externally hosted and akin to a social network. There will be default contexts that can be clonned, users may create contexts for other users or allow for visibility and some control on their personal space. For example, users may want to synchronize how much money they owe eachother. This means databases, load balancers, servers, and so on.

In terms of how much information is to be saved, the objective is to be the maximum possible. Every information that an accountant could want or that is normally stored by a bank are examples. This information is not needed to be all exposed immediatly, as users may have different needs and want different levels of complexity.

The expression of projections into the future is supposed to be rich, making use of dynamic rules that are simulated per date. An example behavior is being able to define a rule for increase of salary by a percentage for an interval of months and a rule that calculates the actual salary based on taxes.

The final form of the app would allow not only to totally describe and project the financial life of a person, including the more complex taxes, but also allow for a user to define objective conditions such as *1.000.000€ in some account in 20 years* and actions such as *selling Y property at variable date* and the app would give possible solutions for the requisites done.
