import { defineConfig } from "vitest/config";
import { loadEnv } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig(({ mode }) => {
  const environment = loadEnv(mode, process.cwd(), "");
  const apiProxyTarget =
    environment.VITE_API_PROXY_TARGET ?? "http://localhost:8080";
  const apiProxyOrigin = new URL(apiProxyTarget).origin;
  const usesLocalApi = apiProxyOrigin === "http://localhost:8080";

  return {
    plugins: [react()],
    server: {
      proxy: {
        "/api": {
          target: apiProxyTarget,
          changeOrigin: true,
          // CloudFront permits its own origin in production. The browser still
          // talks to Vite, so proxy remote requests with the accepted origin.
          headers: usesLocalApi ? {} : { Origin: apiProxyOrigin },
        },
      },
    },
    test: {
      environment: "node",
    },
  };
});
