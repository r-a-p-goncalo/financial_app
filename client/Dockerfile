FROM node:22-alpine AS dependencies

RUN corepack enable

WORKDIR /workspace/client

COPY client/package.json client/pnpm-lock.yaml client/pnpm-workspace.yaml ./
RUN pnpm install --frozen-lockfile

FROM dependencies AS test

COPY client ./
RUN pnpm lint && pnpm test && pnpm build

FROM dependencies AS build

COPY client ./
RUN pnpm build

FROM nginx:1.27-alpine

COPY deploy/nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/client/dist /usr/share/nginx/html

EXPOSE 8080
