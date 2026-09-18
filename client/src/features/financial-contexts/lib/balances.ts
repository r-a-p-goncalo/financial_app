import type { Account, Transaction } from "../../../shared/api/contracts";
import { asDecimal } from "../../../shared/lib/format";

export function accountBalances(accounts: Account[], transactions: Transaction[]) {
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

export function contextTotal(accounts: Account[], transactions: Transaction[]) {
  return [...accountBalances(accounts, transactions).values()]
    .reduce((total, balance) => total.plus(balance), asDecimal(0));
}

export function transactionsNewestFirst(transactions: Transaction[]) {
  return [...transactions].sort((left, right) => {
    const dateComparison = right.dateTime.localeCompare(left.dateTime);
    return dateComparison || right.transactionId.localeCompare(left.transactionId);
  });
}

/**
 * Returns the context-wide total immediately after each transaction. Internal
 * account transfers leave the total unchanged; only external money changes it.
 */
export function transactionRunningTotals(accounts: Account[], transactions: Transaction[]) {
  let total = accounts.reduce((sum, account) => sum.plus(asDecimal(account.initialAmount)), asDecimal(0));
  const totals = new Map<string, ReturnType<typeof asDecimal>>();
  const chronologicalTransactions = [...transactions].sort((left, right) => {
    const dateComparison = left.dateTime.localeCompare(right.dateTime);
    return dateComparison || left.transactionId.localeCompare(right.transactionId);
  });

  for (const transaction of chronologicalTransactions) {
    const value = asDecimal(transaction.value);
    if (!transaction.originAccountId && transaction.targetAccountId) {
      total = total.plus(value);
    } else if (transaction.originAccountId && !transaction.targetAccountId) {
      total = total.minus(value);
    }
    totals.set(transaction.transactionId, total);
  }

  return totals;
}
