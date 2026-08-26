package com.rgoncalo.financialapp.cli.financialcontextview;

import com.rgoncalo.financialapp.cli.CLICommand;
import com.rgoncalo.financialapp.client.ClientApplication;
import com.rgoncalo.financialapp.client.data.financialcontext.FinancialContextView;
import com.rgoncalo.financialapp.client.data.financialcontext.FinancialContextTransactionSummary;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;

import java.util.Scanner;

/**
 * Displays every dated transaction in the financial context, together with
 * the post-transaction total of both affected accounts.
 */
public class CommandListFinancialContextViewAccountTransactions
        extends CLICommand {

    public CommandListFinancialContextViewAccountTransactions(
            ClientApplication app
    ) {
        super(app);
    }

    @Override
    public String commandString() {
        return "transactions-summary";
    }

    @Override
    public void execute(Scanner scanner) {
        FinancialContextView view = app.getCurrentFinancialContextView();

        System.out.println();
        System.out.println("Transactions as of " + view.date() + ":");

        if (view.transactionSummaries().isEmpty()) {
            System.out.println("No transactions found.");
        }

        for (FinancialContextTransactionSummary summary
                : view.transactionSummaries()) {
            TransactionRecord transaction = summary.transaction();

            System.out.println();
            System.out.println(
                    "Id: " + transaction.transactionRecordId()
                            .transactionRecordId()
            );
            System.out.println("Date: " + transaction.dateTime());
            printAccountBalance(
                    "Origin",
                    transaction.originAccountId(),
                    summary.originBalance()
            );
            printAccountBalance(
                    "Target",
                    transaction.targetAccountId(),
                    summary.targetBalance()
            );
            System.out.println("Value: " + transaction.value());
        }

        System.out.println();
    }

    @Override
    public String help() {
        return "lists all transactions with origin and target totals";
    }

    private void printAccountBalance(
            String side,
            AccountRecordId accountId,
            FinancialContextTransactionSummary.AccountBalance balance
    ) {
        if (balance != null) {
            System.out.println(
                    side + ": " + balance.account().account().name()
            );
            System.out.println(
                    side + " total after transaction: "
                            + balance.totalAfterTransaction()
            );
            return;
        }

        System.out.println(
                side + ": " + (accountId == null ? "External" : accountId)
        );
        System.out.println(side + " total after transaction: N/A");
    }
}
