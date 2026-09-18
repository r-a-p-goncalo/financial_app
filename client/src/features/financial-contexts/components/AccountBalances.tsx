import { useMemo } from "react";
import type { Account, Transaction } from "../../../shared/api/contracts";
import { asDecimal, formatAmount } from "../../../shared/lib/format";
import { accountBalances } from "../lib/balances";

interface AccountBalancesProps {
  accounts: Account[];
  transactions: Transaction[];
}

export function AccountBalances({ accounts, transactions }: AccountBalancesProps) {
  const balances = useMemo(() => accountBalances(accounts, transactions), [accounts, transactions]);

  if (accounts.length === 0) {
    return <p className="muted">No accounts have been created yet.</p>;
  }

  return (
    <div className="table-wrap">
      <table>
        <caption className="sr-only">Account balances</caption>
        <thead>
          <tr><th>Account</th><th>Initial amount</th><th>Recorded balance</th></tr>
        </thead>
        <tbody>
          {accounts.map((account) => {
            const balance = balances.get(account.accountId) ?? asDecimal(0);
            return (
              <tr key={account.accountId}>
                <td>{account.name}</td>
                <td>{formatAmount(account.initialAmount)}</td>
                <td className={balance.isNegative() ? "negative" : "amount"}>{formatAmount(balance)}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
