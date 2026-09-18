import { Link, useNavigate, useParams } from "react-router-dom";
import { errorMessage } from "../../../shared/api/api-error";
import { asDecimal, formatAmount } from "../../../shared/lib/format";
import { TransactionDialog } from "../components/TransactionDialog";
import { TransactionList } from "../components/TransactionList";
import { accountBalances } from "../lib/balances";
import { useFinancialContext } from "../queries";

function QueryFailure({ error }: { error: unknown }) {
  return <p className="form-error" role="alert">{errorMessage(error, "Unable to load this account.")}</p>;
}

export function AccountDetailsPage() {
  const { financialContextId, accountId } = useParams();
  const details = useFinancialContext(financialContextId);
  const navigate = useNavigate();

  if (details.isPending) {
    return <section className="panel loading-panel" aria-live="polite">Loading account…</section>;
  }
  if (details.isError) return <QueryFailure error={details.error} />;
  if (!details.data) return null;

  const { financialContext, accounts, transactions } = details.data;
  const account = accounts.find((candidate) => candidate.accountId === accountId);

  if (!account) {
    return (
      <div className="page-stack">
        <Link className="back-link" to={`/contexts/${financialContext.financialContextId}`}>← {financialContext.name}</Link>
        <section className="panel empty-state">
          <p className="eyebrow">Account unavailable</p>
          <h1>We could not find that account.</h1>
          <p>The account may have been removed or the link is no longer valid.</p>
        </section>
      </div>
    );
  }

  const balance = accountBalances(accounts, transactions).get(account.accountId) ?? asDecimal(0);

  return (
    <div className="page-stack">
      <Link className="back-link" to={`/contexts/${financialContext.financialContextId}`}>← {financialContext.name}</Link>
      <section className="context-hero" aria-labelledby="account-title">
        <div>
          <p className="eyebrow">Account</p>
          <h1 id="account-title">{account.name}</h1>
          <p className="muted">Current recorded balance</p>
          <p className={balance.isNegative() ? "hero-amount negative" : "hero-amount"}>{formatAmount(balance)}</p>
        </div>
        <div className="account-amounts">
          <span>Initial amount</span>
          <strong>{formatAmount(account.initialAmount)}</strong>
        </div>
      </section>

      <section className="content-grid" aria-label="Account activity">
        <section className="panel main-panel" aria-labelledby="account-transactions-heading">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Account activity</p>
              <h2 id="account-transactions-heading">Transactions and running balance</h2>
            </div>
          </div>
          <TransactionList accounts={accounts} transactions={transactions} account={account} />
        </section>
        <section className="panel transaction-action-panel" aria-label="Add a transaction for this account">
          <h3>Record activity</h3>
          <p className="field-hint">Income and expense start with this account selected. Transfers can move money to or from another account.</p>
          <TransactionDialog
            financialContextId={financialContext.financialContextId}
            accounts={accounts}
            defaultAccountId={account.accountId}
            onTransactionSaved={(clonedAccountIds) => {
              const clonedAccountId = clonedAccountIds.get(account.accountId);
              if (clonedAccountId) {
                navigate(`/contexts/${financialContext.financialContextId}/accounts/${clonedAccountId}`, { replace: true });
              }
            }}
          />
        </section>
      </section>
    </div>
  );
}
