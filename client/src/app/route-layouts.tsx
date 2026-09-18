import { Outlet, useRouteError } from "react-router-dom";
import { RequireAuthentication } from "../features/auth/route-guards";
import { AppShell } from "./shell";

export function RouteErrorBoundary() {
  const error = useRouteError();
  const message = error instanceof Error ? error.message : "The requested page could not be loaded.";

  return (
    <main className="route-error content-width">
      <p className="eyebrow">Unexpected error</p>
      <h1>We could not load this page.</h1>
      <p>{message}</p>
    </main>
  );
}

export function ProtectedLayout() {
  return (
    <RequireAuthentication>
      <AppShell>
        <Outlet />
      </AppShell>
    </RequireAuthentication>
  );
}
