package com.rgoncalo.financialapp.cli.account;

import com.rgoncalo.financialapp.cli.CLICommand;
import com.rgoncalo.financialapp.client.AccountTransactionSummary;
import com.rgoncalo.financialapp.client.ClientApplication;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;

import java.util.List;
import java.util.Scanner;

/**
 * Displays the loaded account's transactions in time order with the running
 * balance after each transaction.
 */
public class CommandListAccountTransactionSummaries extends CLICommand {

    public CommandListAccountTransactionSummaries(ClientApplication app) {
        super(app);
    }

    @Override
    public String commandString() {
        return "transactions-summary";
    }

    @Override
    public void execute(Scanner scanner) {

        List<AccountTransactionSummary> transactions = app
                .listTransactionsSummaryForCurrentAccount();

        System.out.println();

        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            System.out.println();
            return;
        }

        System.out.println(
                "Transactions for account: "
                        + app.getCurrentAccount().record().name()
        );

        for (AccountTransactionSummary summary : transactions) {
            TransactionRecord transaction = summary.transaction();

            System.out.println();
            System.out.println(
                    "Id: "
                            + transaction.transactionRecordId()
                                    .transactionRecordId()
            );
            System.out.println("Date: " + transaction.dateTime());
            System.out.println("Origin: " + transaction.originAccountId());
            System.out.println("Target: " + transaction.targetAccountId());
            System.out.println("Value: " + transaction.value());
            System.out.println(
                    "Total after transaction: "
                            + summary.totalAfterTransaction()
            );
        }

        System.out.println();
    }

    @Override
    public String help() {
        return "lists account transactions in time order with running totals";
    }
}
