import { type FormEvent, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { errorMessage } from "../../shared/api/api-error";
import type { Account, FinancialContextDetails, Transaction } from "../../shared/api/contracts";
import {
  asDecimal,
  currentDateTimeInputValue,
  dateTimeInputToInstant,
  formatAmount,
  formatDateTime,
} from "../../shared/lib/format";
import {
  useCloneFinancialContext,
  useCreateAccount,
  useCreateFinancialContext,
  useCreateTransaction,
  useFinancialContext,
  useFinancialContexts,
} from "./queries";

function QueryFailure({ error }: { error: unknown }) {
  return <p className="form-error" role="alert">{errorMessage(error, "Unable to load this data.")}</p>;
}

function LoadingPanel() {
  return <section className="panel loading-panel">Loading…</section>;
}

export function ContextsPage() {
  const contexts = useFinancialContexts();
  const createContext = useCreateFinancialContext();
  const navigate = useNavigate();
  const [name, setName] = useState("");

  async function create(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!name.trim()) return;

    const context = await createContext.mutateAsync({ name: name.trim() });
    navigate(`/contexts/${context.financialContextId}`);
  }

  return (
    <div className="page-stack">
      <section className="page-heading">
        <div>
          <p className="eyebrow">Financial contexts</p>
          <h1>Organise every financial life separately.</h1>
          <p className="muted">A context contains accounts, transactions, and the people permitted to access them.</p>
        </div>
        <form className="inline-form" onSubmit={create}>
          <label className="sr-only" htmlFor="context-name">Context name</label>
          <input id="context-name" placeholder="e.g. Personal finances" value={name} onChange={(event) => setName(event.target.value)} required />
          <button className="button button-primary" type="submit" disabled={createContext.isPending}>
            {createContext.isPending ? "Creating…" : "New context"}
          </button>
          {createContext.isError && <span className="form-error" role="alert">{errorMessage(createContext.error)}</span>}
        </form>
      </section>

      {contexts.isPending && <LoadingPanel />}
      {contexts.isError && <QueryFailure error={contexts.error} />}
      {contexts.data && contexts.data.length === 0 && (
        <section className="empty-state panel">
          <h2>Start with one context</h2>
          <p>Your personal budget, a household, or a business can each have its own independent context.</p>
        </section>
      )}
      {contexts.data && contexts.data.length > 0 && (
        <section className="context-grid" aria-label="Your financial contexts">
          {contexts.data.map((context) => (
            <Link className="context-card" key={context.financialContextId} to={`/contexts/${context.financialContextId}`}>
              <span className="context-icon" aria-hidden="true">↗</span>
              <span className="context-card-title">{context.name}</span>
              <span className="muted">{context.parentFinancialContextId ? "Based on a parent context" : "Independent context"}</span>
            </Link>
          ))}
        </section>
      )}
    </div>
  );
}

function AccountBalances({ details }: { details: FinancialContextDetails }) {
  const balances = useMemo(() => accountBalances(details.accounts, details.transactions), [details]);

  if (details.accounts.length === 0) {
    return <p className="muted">No accounts have been created yet.</p>;
  }

  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr><th>Account</th><th>Initial amount</th><th>Recorded balance</th></tr>
        </thead>
        <tbody>
          {details.accounts.map((account) => (
            <tr key={account.accountId}>
              <td>{account.name}</td>
              <td>{formatAmount(account.initialAmount)}</td>
              <td className={balances.get(account.accountId)?.isNegative() ? "negative" : "amount"}>
                {formatAmount(balances.get(account.accountId) ?? asDecimal(0))}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function accountBalances(accounts: Account[], transactions: Transaction[]) {
  const balances = new Map(accounts.map((account) => [account.accountId, asDecimal(account.initialAmount)]));
  for (const transaction of transactions) {
    const value = asDecimal(transaction.value);
    if (transaction.originAccountId) {
      balances.set(transaction.originAccountId, (balances.get(transaction.originAccountId) ?? asDecimal(0)).minus(value));
    }
    if (transaction.targetAccountId) {
      balances.set(transaction.targetAccountId, (balances.get(transaction.targetAccountId) ?? asDecimal(0)).plus(value));
    }
  }
  return balances;
}

function AccountForm({ financialContextId }: { financialContextId: string }) {
  const createAccount = useCreateAccount(financialContextId);
  const [name, setName] = useState("");
  const [initialAmount, setInitialAmount] = useState("0");

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!name.trim() || !initialAmount.trim()) return;
    await createAccount.mutateAsync({ name: name.trim(), initialAmount });
    setName("");
    setInitialAmount("0");
  }

  return (
    <form className="stack-form compact-form" onSubmit={submit}>
      <h3>Add an account</h3>
      <label>Name<input value={name} onChange={(event) => setName(event.target.value)} placeholder="e.g. Everyday account" required /></label>
      <label>Initial amount<input value={initialAmount} onChange={(event) => setInitialAmount(event.target.value)} inputMode="decimal" required /></label>
      <p className="field-hint">Currency/unit support will be added by the API; enter a neutral decimal amount for now.</p>
      {createAccount.isError && <p className="form-error" role="alert">{errorMessage(createAccount.error)}</p>}
      <button className="button button-secondary" type="submit" disabled={createAccount.isPending}>
        {createAccount.isPending ? "Adding…" : "Add account"}
      </button>
    </form>
  );
}

function TransactionForm({ financialContextId, accounts }: { financialContextId: string; accounts: Account[] }) {
  const createTransaction = useCreateTransaction(financialContextId);
  const [originAccountId, setOriginAccountId] = useState("");
  const [targetAccountId, setTargetAccountId] = useState("");
  const [value, setValue] = useState("");
  const [dateTime, setDateTime] = useState(currentDateTimeInputValue);
  const [localError, setLocalError] = useState<string>();

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLocalError(undefined);

    if (!originAccountId && !targetAccountId) {
      setLocalError("Choose an origin or a destination account.");
      return;
    }
    if (originAccountId && originAccountId === targetAccountId) {
      setLocalError("The origin and destination must be different.");
      return;
    }

    try {
      await createTransaction.mutateAsync({
        originAccountId: originAccountId || null,
        targetAccountId: targetAccountId || null,
        dateTime: dateTimeInputToInstant(dateTime),
        value,
      });
      setOriginAccountId("");
      setTargetAccountId("");
      setValue("");
      setDateTime(currentDateTimeInputValue());
    } catch {
      // Mutation state provides the server error.
    }
  }

  const accountOptions = <><option value="">External / not recorded</option>{accounts.map((account) => <option key={account.accountId} value={account.accountId}>{account.name}</option>)}</>;

  return (
    <form className="stack-form compact-form" onSubmit={submit}>
      <h3>Add a transaction</h3>
      <label>Origin account<select value={originAccountId} onChange={(event) => setOriginAccountId(event.target.value)}>{accountOptions}</select></label>
      <label>Destination account<select value={targetAccountId} onChange={(event) => setTargetAccountId(event.target.value)}>{accountOptions}</select></label>
      <label>Amount<input value={value} onChange={(event) => setValue(event.target.value)} inputMode="decimal" required /></label>
      <label>Date and time<input type="datetime-local" value={dateTime} onChange={(event) => setDateTime(event.target.value)} required /></label>
      <p className="field-hint">Use an external side for income or expenses not linked to another recorded account.</p>
      {localError && <p className="form-error" role="alert">{localError}</p>}
      {createTransaction.isError && <p className="form-error" role="alert">{errorMessage(createTransaction.error)}</p>}
      <button className="button button-secondary" type="submit" disabled={createTransaction.isPending || accounts.length === 0}>
        {createTransaction.isPending ? "Adding…" : "Add transaction"}
      </button>
    </form>
  );
}

function TransactionList({ transactions, accounts }: { transactions: Transaction[]; accounts: Account[] }) {
  const accountNames = new Map(accounts.map((account) => [account.accountId, account.name]));
  const orderedTransactions = [...transactions].sort((left, right) => right.dateTime.localeCompare(left.dateTime));

  if (orderedTransactions.length === 0) return <p className="muted">No transactions have been recorded yet.</p>;

  return (
    <div className="table-wrap">
      <table>
        <thead><tr><th>Date</th><th>From</th><th>To</th><th>Amount</th></tr></thead>
        <tbody>
          {orderedTransactions.map((transaction) => (
            <tr key={transaction.transactionId}>
              <td>{formatDateTime(transaction.dateTime)}</td>
              <td>{transaction.originAccountId ? accountNames.get(transaction.originAccountId) ?? "Unavailable account" : "External"}</td>
              <td>{transaction.targetAccountId ? accountNames.get(transaction.targetAccountId) ?? "Unavailable account" : "External"}</td>
              <td className="amount">{formatAmount(transaction.value)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export function ContextDetailsPage() {
  const { financialContextId } = useParams();
  const details = useFinancialContext(financialContextId);
  const cloneContext = useCloneFinancialContext();
  const navigate = useNavigate();

  async function clone() {
    if (!financialContextId || !details.data) return;
    const context = await cloneContext.mutateAsync({
      financialContextId,
      name: `${details.data.financialContext.name} copy`,
    });
    navigate(`/contexts/${context.financialContextId}`);
  }

  if (details.isPending) return <LoadingPanel />;
  if (details.isError) return <QueryFailure error={details.error} />;
  if (!details.data) return null;

  const { financialContext, accounts, transactions } = details.data;
  const totalBalance = [...accountBalances(accounts, transactions).values()]
    .reduce((total, balance) => total.plus(balance), asDecimal(0));

  return (
    <div className="page-stack">
      <Link className="back-link" to="/contexts">← All contexts</Link>
      <section className="context-hero">
        <div>
          <p className="eyebrow">{financialContext.parentFinancialContextId ? "Cloned financial context" : "Financial context"}</p>
          <h1>{financialContext.name}</h1>
          <p className="muted">Recorded total across {accounts.length} {accounts.length === 1 ? "account" : "accounts"}</p>
          <p className={totalBalance.isNegative() ? "hero-amount negative" : "hero-amount"}>{formatAmount(totalBalance)}</p>
        </div>
        <div>
          {cloneContext.isError && <p className="form-error" role="alert">{errorMessage(cloneContext.error)}</p>}
          <button className="button button-quiet" type="button" onClick={clone} disabled={cloneContext.isPending}>
            {cloneContext.isPending ? "Cloning…" : "Clone context"}
          </button>
        </div>
      </section>

      <section className="content-grid">
        <section className="panel main-panel">
          <div className="section-heading"><div><p className="eyebrow">Accounts</p><h2>Balances</h2></div></div>
          <AccountBalances details={details.data} />
        </section>
        <section className="panel"><AccountForm financialContextId={financialContext.financialContextId} /></section>
      </section>

      <section className="content-grid">
        <section className="panel main-panel">
          <div className="section-heading"><div><p className="eyebrow">Activity</p><h2>Transactions</h2></div></div>
          <TransactionList transactions={transactions} accounts={accounts} />
        </section>
        <section className="panel"><TransactionForm financialContextId={financialContext.financialContextId} accounts={accounts} /></section>
      </section>
    </div>
  );
}
