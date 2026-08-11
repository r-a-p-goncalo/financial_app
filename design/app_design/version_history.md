
Here we have a log of previous versions and also a roadmap of future versions.

# App Version 0.1 (Architecture version 0.1)

This version is meant to be a prototype. It is only used locally, testing the logic without any of the server-client relation meant to exist later. There is only a single app, which is interactable only through commands in the command-line.

## Features

Its features are:
- Basic account, account group, expenses, expense category interaction in a financial context.

- Extending financial contexts and other types of financial entities, with financial views.

- Minimal functionality with rules to creates expenses across time.
    - transaction creation rule layout which create a transaction with tags, with targets, with base value, with percentage value change (0% means no change), with an interval of time between each change.

- Generation of statistics and graphs
    - a bar graph that shows, for a single account, for each month, the values for each root tag

    - a graph that shows the evolution of the total value for each account

## Limitations

We are to implement only the necessary system to achieve the desire features. Any more is over-reach.

Programs, assets, entities are omitted.

There are no loose copies, any clonning of data is deep.

### General usage

* Only a single user is supported. Authentication, authorization, roles, and permissions are omitted.

* The application is local-only. There is no server, API, networking, or client-server communication.

* Persistence is not a requirement for this version. Data may exist only for the duration of the application process.

### Transactions

* Composite transactions are omitted.

* Account groups are omitted.

* Currency conversion is omitted. Transactions involving monetary values must use compatible currencies.

* Programs, assets, multiple entities

* Transaction creation rules support only the minimal recurring transaction functionality defined for this version.


## Technology

The application will be implemented in Java.

The database will be implemented in sqlite.

# App Version 0.2



