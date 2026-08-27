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

            printTransactionInfo(summary);

        }

        System.out.println();
    }

    @Override
    public String help() {
        return "lists all transactions with origin and target totals";
    }

    private void printTransactionInfo(FinancialContextTransactionSummary summary){

        TransactionRecord transaction = summary.transaction();

        System.out.println();

        System.out.println("Id: " + transaction.transactionRecordId().transactionRecordId());

        System.out.println("Date: " + transaction.dateTime());

        String originName = displayAccount(transaction.originAccountId());
        String targetName = displayAccount(transaction.targetAccountId());

        System.out.println(originName + " -> " + transaction.value() + " -> " + targetName);

        if (transaction.originAccountId() != null)
            printAccountBalance(originName, transaction.originAccountId(), summary.originBalance());

        if (transaction.targetAccountId() != null)
            printAccountBalance( targetName, transaction.targetAccountId(), summary.targetBalance());

    }

    private void printAccountBalance(
            String side,
            AccountRecordId accountId,
            FinancialContextTransactionSummary.AccountBalance balance
    ) {
        if (balance != null) {
            System.out.println(side + " total after transaction: " + balance.totalAfterTransaction());
            return;
        }

        System.out.println(side + ": " + (accountId == null ? "External" : accountId));

        System.out.println(side + " total after transaction: N/A");
    }

    private String displayAccount(AccountRecordId accountRecordId) {
        if (accountRecordId == null) {
            return UNKNOWN_OR_IRRELEVANT_ACCOUNT;
        }

        return app.dataCache().getCachedAccountRecordName(accountRecordId, accountRecordId.toString());
    }
}
