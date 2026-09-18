import { createBrowserRouter, Navigate } from "react-router-dom";
import { ProtectedLayout, RouteErrorBoundary } from "./route-layouts";
import { LoginPage, RegisterPage } from "../features/auth/auth-pages";
import { PublicOnly } from "../features/auth/route-guards";
import { ContextDetailsPage, ContextsPage } from "../features/financial-contexts/context-pages";

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
    ],
  },
  { path: "*", element: <Navigate to="/" replace /> },
]);
