#uses Node.js only for installing dependencies, testing, and building the frontend
FROM node:22-alpine AS dependencies

#enables pnpm through the Node.js Corepack tool
RUN corepack enable

#sets the temporary frontend build workspace
WORKDIR /workspace/client

#copies dependency files first, so Docker can cache the pnpm install step
COPY client/package.json client/pnpm-lock.yaml client/pnpm-workspace.yaml ./

#installs the exact dependency versions recorded in the lock file
RUN pnpm install --frozen-lockfile

#creates the CI test stage from the installed dependencies
FROM dependencies AS test

#copies the frontend source code into the test workspace
COPY client ./

#checks code style, runs tests, and confirms the production frontend can be built
RUN pnpm lint && pnpm test && pnpm build

#creates a production build stage from the installed dependencies
FROM dependencies AS build

#copies the frontend source code into the production build workspace
COPY client ./

#the API origin is compiled into the static browser bundle for deployments
ARG VITE_API_BASE_URL
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

#creates static files for the private S3 client bucket
RUN pnpm build

#exports only the Vite build output when Docker's local exporter is used;
#the dependency store and TypeScript source never become deployment artifacts
FROM scratch AS artifacts
COPY --from=build /workspace/client/dist/ /
