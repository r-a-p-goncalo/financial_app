import { useMemo } from "react";
import type { Account, Transaction } from "../../../shared/api/contracts";
import { asDecimal, formatAmount, formatDateTime } from "../../../shared/lib/format";
import { transactionRunningTotals, transactionsNewestFirst } from "../lib/balances";

interface TransactionListProps {
  accounts: Account[];
  transactions: Transaction[];
}

export function TransactionList({ accounts, transactions }: TransactionListProps) {
  const accountNames = useMemo(() => new Map(accounts.map((account) => [account.accountId, account.name])), [accounts]);
  const orderedTransactions = useMemo(() => transactionsNewestFirst(transactions), [transactions]);
  const runningTotals = useMemo(() => transactionRunningTotals(accounts, transactions), [accounts, transactions]);

  if (orderedTransactions.length === 0) {
    return <p className="muted">No transactions have been recorded yet.</p>;
  }

  return (
    <div className="table-wrap">
      <table>
        <caption className="sr-only">Transactions and their context totals</caption>
        <thead><tr><th>Date</th><th>From</th><th>To</th><th>Amount</th><th>Running total</th></tr></thead>
        <tbody>
          {orderedTransactions.map((transaction) => {
            const runningTotal = runningTotals.get(transaction.transactionId) ?? asDecimal(0);
            return (
              <tr key={transaction.transactionId}>
                <td>{formatDateTime(transaction.dateTime)}</td>
                <td>{transaction.originAccountId ? accountNames.get(transaction.originAccountId) ?? "Unavailable account" : "External"}</td>
                <td>{transaction.targetAccountId ? accountNames.get(transaction.targetAccountId) ?? "Unavailable account" : "External"}</td>
                <td className="amount">{formatAmount(transaction.value)}</td>
                <td className={runningTotal.isNegative() ? "negative" : "amount"}>{formatAmount(runningTotal)}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
