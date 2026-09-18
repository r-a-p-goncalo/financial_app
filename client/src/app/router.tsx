import { createBrowserRouter, Navigate } from "react-router-dom";
import { ProtectedLayout, RouteErrorBoundary } from "./route-layouts";
import { LoginPage, RegisterPage } from "../features/auth/auth-pages";
import { PublicOnly } from "../features/auth/route-guards";
import { AccountDetailsPage } from "../features/financial-contexts/pages/AccountDetailsPage";
import { ContextDetailsPage } from "../features/financial-contexts/pages/ContextDetailsPage";
import { ContextsPage } from "../features/financial-contexts/pages/ContextsPage";

export const router = createBrowserRouter([
  {
    path: "/login",
    element: (
      <PublicOnly>
        <LoginPage />
      </PublicOnly>
    ),
  },
  {
    path: "/register",
    element: (
      <PublicOnly>
        <RegisterPage />
      </PublicOnly>
    ),
  },
  {
    path: "/",
    element: <ProtectedLayout />,
    errorElement: <RouteErrorBoundary />,
    children: [
      { index: true, element: <Navigate to="/contexts" replace /> },
      { path: "contexts", element: <ContextsPage /> },
      { path: "contexts/:financialContextId", element: <ContextDetailsPage /> },
      { path: "contexts/:financialContextId/accounts/:accountId", element: <AccountDetailsPage /> },
    ],
  },
  { path: "*", element: <Navigate to="/" replace /> },
]);
