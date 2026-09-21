# Production package

This Compose stack runs one Spring Boot instance connected to an external
Aurora PostgreSQL cluster. The frontend container serves the React build and
forwards `/api/*` and `/actuator/health` internally to the application
container. No database files are stored on the EC2 host.

TLS is intentionally terminated outside this stack. On a single EC2 host, run
a TLS reverse proxy such as Caddy on the host and proxy it to
`127.0.0.1:8080`. With an Application Load Balancer, set
`FRONTEND_BIND_ADDRESS=0.0.0.0` and allow that port only from the load
balancer's security group. Do not expose the Spring Boot port or the Aurora
PostgreSQL port to the internet.

## First deployment

1. Create an Aurora PostgreSQL cluster and a least-privilege
   `financial_app` database user. Keep the cluster private in the same VPC as
   the application host. Its security group may allow port `5432` only from
   the application's security group.
2. Download the current AWS RDS CA bundle to the host, then copy
   `.env.example` to `.env`. Set `DATABASE_JDBC_URL` to the cluster writer
   endpoint, retain `sslmode=verify-full`, and set
   `DATABASE_CA_CERTIFICATE_FILE` to the downloaded bundle. Do not commit
   `.env`; source `DATABASE_PASSWORD` from your secret-management process.
3. Configure the TLS proxy or load balancer to forward the original
   `X-Forwarded-Proto: https` header.
4. From the repository root, build and start the stack:

   ```text
   docker compose --env-file deploy/.env -f deploy/compose.production.yaml up --build -d
   ```

5. Verify the proxy can reach `GET /actuator/health`, then register a test
   user and exercise login, context creation, and transaction creation.

The application applies its versioned PostgreSQL migrations at startup. Take
an Aurora snapshot before production migrations and use backward-compatible
schema changes so the preceding application image can still be restored.

## CI verification

The CI workflow runs the Java repository contract tests against a disposable
local PostgreSQL container, then validates the client lint, tests, production
build, and runtime images. PostgreSQL parity tests run directly on the CI
worker because Testcontainers requires access to Docker.
