# Financial App client

The browser client is a Vite + React + TypeScript single-page application for
the Spring Boot API in `../app`.

## Run locally

1. Start the API from `../app` with `mvn spring-boot:run`.
2. From this directory, install packages with `pnpm install`.
3. Run `pnpm dev` and open the URL printed by Vite (normally
   `http://localhost:5173`).

By default, Vite proxies `/api` to `http://localhost:8080`. This keeps browser
session cookies and CSRF requests same-origin in development. The API uses its
local SQLite database by default, so the local client and API can be used
without accessing deployed data.

## Run the local client against the deployed API

The deployed API is available to browsers only through the CloudFront frontend
endpoint. Do not set `VITE_API_BASE_URL` to that endpoint: direct browser
requests would be cross-origin and would not have a usable production session.
Instead, let Vite proxy the API requests.

Create an untracked `client/.env.remote` file from `.env.example` and replace
the example value with the CloudFront frontend URL:

```text
VITE_API_PROXY_TARGET=https://your-distribution.cloudfront.net
```

Then start Vite in remote mode:

```text
pnpm dev -- --mode remote
```

On Windows, `../scripts/Start-ClientRemote.ps1` runs this mode. It accepts
`-ApiProxyTarget` for a one-off CloudFront URL or reads the untracked
`.env.remote` file. Add `-InstallDependencies` on its first run to install the
locked client dependencies.

The browser still calls same-origin `/api/v1` on the Vite server, which forwards
the requests to CloudFront. The proxy presents CloudFront's origin to the API
for its production CORS policy, while browser session and CSRF cookies remain
local to the Vite origin. Remote mode reads and writes the deployed database,
so use a dedicated test account. If a browser rejects the deployed API's
secure cookies on HTTP localhost, run Vite over local HTTPS for this mode.

The AWS deployment serves the client through CloudFront and forwards the
same-origin `/api/*` route to the Spring service. CloudFront rewrites
client-side routes such as `/contexts/:id` to `index.html`.

## Run the complete local stack on Windows

`../scripts/Start-LocalStack.ps1` opens two PowerShell 7 terminals: the API
from `app` and the Vite client from `client`. The client uses the default Vite
proxy to reach `http://localhost:8080`; the API uses the local SQLite database
at `app/data/financial-app.db`, not the deployed database. It loads the known
`demo` / `demo-password` scenario by default. Add `-ResetDatabase` to recreate
that local database and its demo data, or `-BootstrapConfigFile` to select
another JSON scenario. Add `-InstallDependencies` on the first run if client
dependencies are not already installed.

## Architecture

- `src/shared/api/contracts.ts` contains framework-independent HTTP types.
- `src/shared/api/api-client.ts` contains platform-independent operations.
- `src/shared/api/browser-transport.ts` adds the browser session, cookie, and
  CSRF behavior required by the current Spring Security setup.
- `src/features` owns pages, UI components, mutations, and query invalidation
  by product feature.
- `src/features/financial-contexts/pages` coordinates routing, loading and
  error states; `components` renders the account and transaction workflows;
  `lib/balances.ts` holds the tested, framework-independent financial
  calculations.
- `src/shared/lib/format.ts` owns decimal validation and presentation so the
  UI never performs money calculations with JavaScript floating-point values.

When the API publishes an OpenAPI contract, replace `contracts.ts` and the
operation layer with generated code without changing feature screens. A future
React Native client can reuse those generated types and operations but supply a
native transport and mobile authentication flow.

## Current product scope

The client implements the REST API's currently available part of the product
design: authenticated users can create independent financial contexts (or
create a live clone for a scenario), add accounts, and record dated transfers.
A clone continues to receive accounts and transactions added to its parent;
child-specific records are stored only when the child changes or adds data. A
transaction may have one external side, which represents income or an expense.
An inherited account carries its owning context ID, so the client recognizes it
as virtual. Before posting a child transaction that uses that account, the
client explicitly requests a real child account copy and uses the returned ID.
The account table and each account's transaction running balance are derived
from the same decimal-safe financial rules. Transactions are recorded through
an income, expense, or transfer workflow; leaving the optional date empty uses
the date and time at which the transaction is saved.

The broader product design also calls for tags, category analysis, dated
simulations, recurring rules, currency units, and shared-context permissions.
Those are deliberately not represented in the client until the REST API makes
the corresponding domain data and operations available.

## Validation

```text
pnpm lint
pnpm test
pnpm build
```

Currency is deliberately not displayed in the UI because the API does not yet
model a monetary unit. Amounts remain neutral decimals until that domain feature
is added.
