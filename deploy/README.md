# Production package

This Compose stack runs one Spring Boot instance with SQLite on a host-mounted
data directory. The frontend container serves the React build and forwards
`/api/*` and `/actuator/health` internally to the application container.

TLS is intentionally terminated outside this stack. On a single EC2 host, run
a TLS reverse proxy such as Caddy on the host and proxy it to
`127.0.0.1:8080`. With an Application Load Balancer, set
`FRONTEND_BIND_ADDRESS=0.0.0.0` and allow that port only from the load
balancer's security group. Do not expose the Spring Boot port or SQLite files.

## First deployment

1. Copy `.env.example` to `.env` and replace `PUBLIC_ORIGIN` with the final
   HTTPS origin.
2. Mount persistent storage at `DATA_DIRECTORY`, create the directory, and
   make it writable by the image's application user (`uid` and `gid` `10001`).
3. Configure the TLS proxy or load balancer to forward the original
   `X-Forwarded-Proto: https` header.
4. From the repository root, build and start the stack:

   ```text
   docker compose --env-file deploy/.env -f deploy/compose.production.yaml up --build -d
   ```

5. Verify the proxy can reach `GET /actuator/health`, then register a test
   user and exercise login, context creation, and transaction creation.

The `DATA_DIRECTORY` is the only persisted application state. Back it up
before each deployment and test restoring a backup before storing real data.

## CI verification

The application and client Dockerfiles expose `test` build stages. They run
the Java test suite and the client lint, test, and build commands with the
same Java and Node versions used for packaging.
