import { describe, expect, it } from "vitest";
import type { Account, Transaction } from "../../../shared/api/contracts";
import {
  accountBalances,
  accountTransactionRunningTotals,
  contextTotal,
  transactionsNewestFirst,
} from "./balances";

const accounts: Account[] = [
  { accountId: "cash", name: "Cash", initialAmount: "100" },
  { accountId: "savings", name: "Savings", initialAmount: "50" },
];

const transactions: Transaction[] = [
  { transactionId: "income", originAccountId: null, targetAccountId: "cash", dateTime: "2026-01-01T10:00:00Z", value: "40" },
  { transactionId: "transfer", originAccountId: "cash", targetAccountId: "savings", dateTime: "2026-01-02T10:00:00Z", value: "25" },
  { transactionId: "expense", originAccountId: "savings", targetAccountId: null, dateTime: "2026-01-03T10:00:00Z", value: "10" },
];

describe("financial context balances", () => {
  it("derives account and context totals from initial amounts and transfers", () => {
    const balances = accountBalances(accounts, transactions);

    expect(balances.get("cash")?.toString()).toBe("115");
    expect(balances.get("savings")?.toString()).toBe("65");
    expect(contextTotal(accounts, transactions).toString()).toBe("180");
  });

  it("orders transaction history without changing the input order", () => {
    expect(transactionsNewestFirst(transactions).map((transaction) => transaction.transactionId))
      .toEqual(["expense", "transfer", "income"]);
    expect(transactions.map((transaction) => transaction.transactionId))
      .toEqual(["income", "transfer", "expense"]);
  });

  it("records a running total only for the selected account", () => {
    const totals = accountTransactionRunningTotals(accounts[1], transactions);

    expect(totals.has("income")).toBe(false);
    expect(totals.get("transfer")?.toString()).toBe("75");
    expect(totals.get("expense")?.toString()).toBe("65");
  });
});
