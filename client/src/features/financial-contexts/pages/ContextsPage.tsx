import { type FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { errorMessage } from "../../../shared/api/api-error";
import { useCreateFinancialContext, useFinancialContexts } from "../queries";

function QueryFailure({ error }: { error: unknown }) {
  return <p className="form-error" role="alert">{errorMessage(error, "Unable to load your financial contexts.")}</p>;
}

export function ContextsPage() {
  const contexts = useFinancialContexts();
  const createContext = useCreateFinancialContext();
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [localError, setLocalError] = useState<string>();

  async function create(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLocalError(undefined);

    if (!name.trim()) {
      setLocalError("Enter a name for the financial context.");
      return;
    }

    try {
      const context = await createContext.mutateAsync({ name: name.trim() });
      navigate(`/contexts/${context.financialContextId}`);
    } catch {
      // Mutation state provides the server error.
    }
  }

  return (
    <div className="page-stack">
      <section className="page-heading" aria-labelledby="contexts-title">
        <div>
          <p className="eyebrow">Financial contexts</p>
          <h1 id="contexts-title">Organise every financial life separately.</h1>
          <p className="muted">Create a workspace for your personal finances, a household, or a business. Each context keeps its accounts and transaction history independent.</p>
        </div>
        <form className="inline-form" onSubmit={create} noValidate>
          <label className="sr-only" htmlFor="context-name">Context name</label>
          <input id="context-name" placeholder="e.g. Personal finances" value={name} onChange={(event) => setName(event.target.value)} required />
          <button className="button button-primary" type="submit" disabled={createContext.isPending}>
            {createContext.isPending ? "Creating…" : "New context"}
          </button>
          {localError && <span className="form-error" role="alert">{localError}</span>}
          {createContext.isError && <span className="form-error" role="alert">{errorMessage(createContext.error)}</span>}
        </form>
      </section>

      {contexts.isPending && <section className="panel loading-panel" aria-live="polite">Loading your financial contexts…</section>}
      {contexts.isError && <QueryFailure error={contexts.error} />}
      {contexts.data && contexts.data.length === 0 && (
        <section className="empty-state panel">
          <h2>Start with one context</h2>
          <p>Create a context to record the accounts and transactions you want to track together.</p>
        </section>
      )}
      {contexts.data && contexts.data.length > 0 && (
        <section className="context-grid" aria-label="Your financial contexts">
          {contexts.data.map((context) => (
            <Link className="context-card" key={context.financialContextId} to={`/contexts/${context.financialContextId}`}>
              <span className="context-icon" aria-hidden="true">↗</span>
              <span className="context-card-title">{context.name}</span>
              <span className="muted">{context.parentFinancialContextId ? "A branch of another context" : "Independent context"}</span>
            </Link>
          ))}
        </section>
      )}
    </div>
  );
}
