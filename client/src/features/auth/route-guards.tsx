import type { PropsWithChildren } from "react";
import { Navigate } from "react-router-dom";
import { errorMessage, isUnauthenticated } from "../../shared/api/api-error";
import { useSession } from "./session";

function SessionLoading() {
  return <main className="centered-status">Checking your session…</main>;
}

function SessionFailure({ message }: { message: string }) {
  return (
    <main className="centered-status">
      <p className="eyebrow">Connection problem</p>
      <h1>We could not reach your workspace.</h1>
      <p>{message}</p>
    </main>
  );
}

export function RequireAuthentication({ children }: PropsWithChildren) {
  const session = useSession();

  if (session.isPending) return <SessionLoading />;
  if (isUnauthenticated(session.error)) return <Navigate to="/login" replace />;
  if (session.isError) return <SessionFailure message={errorMessage(session.error)} />;
  return <>{children}</>;
}

export function PublicOnly({ children }: PropsWithChildren) {
  const session = useSession();

  if (session.isPending) return <SessionLoading />;
  if (session.data) return <Navigate to="/contexts" replace />;
  if (session.isError && !isUnauthenticated(session.error)) {
    return <SessionFailure message={errorMessage(session.error)} />;
  }
  return <>{children}</>;
}
