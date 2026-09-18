import { useMemo } from "react";
import type { Account, Transaction } from "../../../shared/api/contracts";
import { asDecimal, formatAmount, formatDateTime } from "../../../shared/lib/format";
import { accountTransactionRunningTotals, transactionsNewestFirst } from "../lib/balances";

interface TransactionListProps {
  accounts: Account[];
  transactions: Transaction[];
  account?: Account;
}

export function TransactionList({ accounts, transactions, account }: TransactionListProps) {
  const accountNames = useMemo(() => new Map(accounts.map((account) => [account.accountId, account.name])), [accounts]);
  const visibleTransactions = useMemo(
    () => account
      ? transactions.filter((transaction) => transaction.originAccountId === account.accountId || transaction.targetAccountId === account.accountId)
      : transactions,
    [account, transactions],
  );
  const orderedTransactions = useMemo(() => transactionsNewestFirst(visibleTransactions), [visibleTransactions]);
  const runningTotals = useMemo(
    () => account ? accountTransactionRunningTotals(account, transactions) : undefined,
    [account, transactions],
  );

  if (orderedTransactions.length === 0) {
    return <p className="muted">No transactions have been recorded yet.</p>;
  }

  return (
    <div className="table-wrap">
      <table>
        <caption className="sr-only">{account ? `${account.name} transactions and running balance` : "Transactions"}</caption>
        <thead><tr><th>Date</th><th>From</th><th>To</th><th>Amount</th>{account && <th>Account balance</th>}</tr></thead>
        <tbody>
          {orderedTransactions.map((transaction) => {
            const runningTotal = runningTotals?.get(transaction.transactionId) ?? asDecimal(0);
            return (
              <tr key={transaction.transactionId}>
                <td>{formatDateTime(transaction.dateTime)}</td>
                <td>{transaction.originAccountId ? accountNames.get(transaction.originAccountId) ?? "Unavailable account" : "External"}</td>
                <td>{transaction.targetAccountId ? accountNames.get(transaction.targetAccountId) ?? "Unavailable account" : "External"}</td>
                <td className="amount">{formatAmount(transaction.value)}</td>
                {account && <td className={runningTotal.isNegative() ? "negative" : "amount"}>{formatAmount(runningTotal)}</td>}
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
