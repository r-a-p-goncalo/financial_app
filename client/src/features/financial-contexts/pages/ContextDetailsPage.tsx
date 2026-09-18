import { Link, useNavigate, useParams } from "react-router-dom";
import { errorMessage } from "../../../shared/api/api-error";
import { formatAmount } from "../../../shared/lib/format";
import { AccountBalances } from "../components/AccountBalances";
import { AccountForm } from "../components/AccountForm";
import { TransactionForm } from "../components/TransactionForm";
import { TransactionList } from "../components/TransactionList";
import { contextTotal } from "../lib/balances";
import { useCloneFinancialContext, useFinancialContext } from "../queries";

function QueryFailure({ error }: { error: unknown }) {
  return <p className="form-error" role="alert">{errorMessage(error, "Unable to load this financial context.")}</p>;
}

export function ContextDetailsPage() {
  const { financialContextId } = useParams();
  const details = useFinancialContext(financialContextId);
  const cloneContext = useCloneFinancialContext();
  const navigate = useNavigate();

  async function clone() {
    if (!financialContextId || !details.data) return;

    try {
      const context = await cloneContext.mutateAsync({
        financialContextId,
        name: `${details.data.financialContext.name} copy`,
      });
      navigate(`/contexts/${context.financialContextId}`);
    } catch {
      // Mutation state provides the server error.
    }
  }

  if (details.isPending) {
    return <section className="panel loading-panel" aria-live="polite">Loading financial context…</section>;
  }
  if (details.isError) return <QueryFailure error={details.error} />;
  if (!details.data) return null;

  const { financialContext, accounts, transactions } = details.data;
  const total = contextTotal(accounts, transactions);

  return (
    <div className="page-stack">
      <Link className="back-link" to="/contexts">← All contexts</Link>
      <section className="context-hero" aria-labelledby="context-title">
        <div>
          <p className="eyebrow">{financialContext.parentFinancialContextId ? "Financial context branch" : "Financial context"}</p>
          <h1 id="context-title">{financialContext.name}</h1>
          <p className="muted">Recorded total across {accounts.length} {accounts.length === 1 ? "account" : "accounts"}</p>
          <p className={total.isNegative() ? "hero-amount negative" : "hero-amount"}>{formatAmount(total)}</p>
        </div>
        <div className="hero-actions">
          {cloneContext.isError && <p className="form-error hero-error" role="alert">{errorMessage(cloneContext.error)}</p>}
          <button className="button button-quiet" type="button" onClick={clone} disabled={cloneContext.isPending}>
            {cloneContext.isPending ? "Cloning…" : "Clone context"}
          </button>
        </div>
      </section>

      <section className="content-grid" aria-label="Accounts">
        <section className="panel main-panel" aria-labelledby="accounts-heading">
          <div className="section-heading"><div><p className="eyebrow">Accounts</p><h2 id="accounts-heading">Balances</h2></div></div>
          <AccountBalances accounts={accounts} transactions={transactions} />
        </section>
        <section className="panel" aria-label="Add an account"><AccountForm financialContextId={financialContext.financialContextId} /></section>
      </section>

      <section className="content-grid" aria-label="Transactions">
        <section className="panel main-panel" aria-labelledby="transactions-heading">
          <div className="section-heading"><div><p className="eyebrow">Activity</p><h2 id="transactions-heading">Transactions</h2></div></div>
          <TransactionList accounts={accounts} transactions={transactions} />
        </section>
        <section className="panel" aria-label="Record a transaction"><TransactionForm financialContextId={financialContext.financialContextId} accounts={accounts} /></section>
      </section>
    </div>
  );
}
