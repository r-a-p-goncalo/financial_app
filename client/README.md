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
- `src/features` owns screens, mutations, and query invalidation by product
  feature.

When the API publishes an OpenAPI contract, replace `contracts.ts` and the
operation layer with generated code without changing feature screens. A future
React Native client can reuse those generated types and operations but supply a
native transport and mobile authentication flow.

## Validation

```text
pnpm lint
pnpm test
pnpm build
```

Currency is deliberately not displayed in the UI because the API does not yet
model a monetary unit. Amounts remain neutral decimals until that domain feature
is added.
