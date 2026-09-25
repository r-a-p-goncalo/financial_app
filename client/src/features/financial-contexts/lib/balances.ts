import type { Account, Transaction } from "../../../shared/api/contracts";
import { asDecimal } from "../../../shared/lib/format";

/**
 *
 * Calculates account balances given the accounts and their transactions
 *
 * @param accounts
 * @param transactions
 */
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

/**
 *
 * Calculates the total value of a financial context given the accounts and transactions
 *
 * @param accounts
 * @param transactions
 */
export function contextTotal(accounts: Account[], transactions: Transaction[]) {
  return [...accountBalances(accounts, transactions).values()]
    .reduce((total, balance) => total.plus(balance), asDecimal(0));
}

/**
 *
 * Sorts the given transactions using the date time, returning a new array
 *
 * It uses {@link inplaceTransactionsNewestFirst}
 */
export function transactionsNewestFirst(transactions: Transaction[]) {
  return inplaceTransactionsNewestFirst([...transactions]);
}

/**
 *
 * Sorts the transactions by datetime in the given array, using the ID in the case of ties
 *
 * @param transactions
 */
export function inplaceTransactionsNewestFirst(transactions: Transaction[]) {
  return transactions.sort((left, right) => {

    // <0 if left comes before right >0 if right comes before left
    const dateComparison = right.dateTime.localeCompare(left.dateTime);

    //returns the date comparison if it is non 0, else uses the ID to order
    return dateComparison || right.transactionId.localeCompare(left.transactionId);

  });
}

/**
 *
 * Returns a new array of transactions that has only the transactions that affect the given account
 *
 * @param account
 * @param transactions
 */
function filterTransactionsAffectingAccount(account: Account, transactions: Transaction[]){

  return transactions.filter( (transaction) => transaction.originAccountId == account.accountId
                                                                  ||  transaction.targetAccountId == account.accountId);

}

/**
 *
 * Returns the account balance immediately after each transaction affecting it.
 *
 * @param account
 * @param transactions the ordered transactions
 */
export function accountTransactionRunningTotals(account: Account, transactions: Transaction[]) {

  //we store the current total, as we add the transactions
  let total = asDecimal(account.initialAmount);

  //here we store the total for each transaction
  const totals = new Map<string, ReturnType<typeof asDecimal>>();

  //we filter for the target account and sort the transactions
  const chronologicalTransactions = inplaceTransactionsNewestFirst(filterTransactionsAffectingAccount(account, transactions));

  //we then compute, for each transaction, the
  for (const transaction of chronologicalTransactions) {

    const value = asDecimal(transaction.value);
    if (transaction.originAccountId === account.accountId) total = total.minus(value);
    if (transaction.targetAccountId === account.accountId) total = total.plus(value);
    totals.set(transaction.transactionId, total);

  }

  return totals;
}
