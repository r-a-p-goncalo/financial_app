import type { PropsWithChildren } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useLogout, useSession } from "../features/auth/session";

export function AppShell({ children }: PropsWithChildren) {
  const session = useSession();
  const logout = useLogout();
  const navigate = useNavigate();

  async function handleLogout() {
    await logout.mutateAsync();
    navigate("/login", { replace: true });
  }

  return (
    <div className="app-frame">
      <header className="topbar">
        <NavLink className="brand" to="/contexts" aria-label="Financial App home">
          <span className="brand-mark" aria-hidden="true">F</span>
          <span>Financial App</span>
        </NavLink>
        <nav className="topbar-actions" aria-label="Primary navigation">
          <NavLink className="nav-link" to="/contexts">Contexts</NavLink>
          <span className="user-name">{session.data?.name}</span>
          <button className="button button-quiet" type="button" onClick={handleLogout} disabled={logout.isPending}>
            {logout.isPending ? "Signing out…" : "Sign out"}
          </button>
        </nav>
      </header>
      <main className="content-width page-content">{children}</main>
    </div>
  );
}
