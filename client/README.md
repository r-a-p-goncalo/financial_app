# Financial App client

The browser client is a Vite + React + TypeScript single-page application for
the Spring Boot API in `../app`.

## Run locally

1. Start the API from `../app` with `mvn spring-boot:run`.
2. From this directory, install packages with `pnpm install`.
3. Run `pnpm dev` and open the URL printed by Vite (normally
   `http://localhost:5173`).

Vite proxies `/api` to `http://localhost:8080`. This keeps browser session
cookies and CSRF requests same-origin in development. To point the built client
at another API host, set `VITE_API_BASE_URL`, for example:

```text
VITE_API_BASE_URL=https://api.example.com/api/v1
```

For deployment, prefer a same-origin route such as `/api/*` forwarded to the
Spring service by the reverse proxy or CloudFront. Configure the static host to
fall back to `index.html` for client-side routes such as `/contexts/:id`.

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
clone one for a scenario), add accounts, and record dated transfers. A
transaction may have one external side, which represents income or an expense.
The account table and the transaction running total are derived from the same
decimal-safe financial rules.

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
