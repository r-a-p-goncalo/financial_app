package com.rgoncalo.financialapp.cli.transaction;

import com.rgoncalo.financialapp.cli.CLICommand;
import com.rgoncalo.financialapp.client.ClientApplication;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;

import java.util.Collection;
import java.util.Scanner;

public class CommandListTransactionSummaries
        extends CLICommand {

    public CommandListTransactionSummaries(
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

        Collection<TransactionRecord> transactions =
                app.listTransactionsSummary();

        System.out.println();

        if (transactions.isEmpty()) {

            System.out.println(
                    "No transactions found."
            );

            System.out.println();
            return;
        }

        System.out.println("Transactions:");

        for (TransactionRecord transaction : transactions) {

            System.out.println();

            System.out.println(
                    "Id: " +
                            transaction.transactionRecordId().transactionRecordId()
            );

            System.out.println(
                    "Origin: " +
                            transaction.originAccountId()
            );

            System.out.println(
                    "Target: " +
                            transaction.targetAccountId()
            );

            System.out.println(
                    "Date: " +
                            transaction.dateTime()
            );

            System.out.println(
                    "Value: " +
                            transaction.value()
            );
        }

        System.out.println();
    }

    @Override
    public String help() {
        return "lists transactions in the current financial context";
    }
}