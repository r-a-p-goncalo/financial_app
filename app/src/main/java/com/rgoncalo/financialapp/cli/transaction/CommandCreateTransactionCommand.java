package com.rgoncalo.financialapp.cli.transaction;

import com.rgoncalo.financialapp.cli.CLICommand;
import com.rgoncalo.financialapp.client.ClientApplication;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;

import java.time.Instant;
import java.util.Scanner;

public class CommandCreateTransactionCommand
        extends CLICommand {

    public CommandCreateTransactionCommand(
            ClientApplication app
    ) {
        super(app);
    }

    @Override
    public String commandString() {
        return "create-transaction";
    }

    @Override
    public void execute(Scanner scanner) {

        System.out.println("Creating transaction.");

        AccountRecordId originAccountId = null;
        System.out.print("Origin account id: ");
        String originAccountIdString =
                scanner.nextLine().trim();

        if(!originAccountIdString.isEmpty())
            originAccountId =  app.getAccountRecordIdIdFrom(originAccountIdString);

        AccountRecordId targetAccountId = null;
        System.out.print("Target account id: ");
        String targetAccountIdString =
                scanner.nextLine().trim();

        if(!targetAccountIdString.isEmpty())
            targetAccountId = app.getAccountRecordIdIdFrom(targetAccountIdString);

        System.out.print("Amount: ");
        String amount =
                scanner.nextLine().trim();

        MonetaryValue value =
                new MonetaryValue(
                        Double.parseDouble(amount)
                );


        TransactionRecord transaction =
                app.createTransaction(
                        originAccountId,
                        targetAccountId,
                        Instant.now(),
                        value
                );

        System.out.println();
        System.out.println("Transaction created.");
        System.out.println(
                "Id: " +
                        transaction.transactionRecordId().transactionRecordId()
        );
        System.out.println(
                "Origin account: " +
                        transaction.originAccountId()
        );
        System.out.println(
                "Target account: " +
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
        System.out.println();
    }

    @Override
    public String help() {
        return "creates a transaction between two accounts";
    }
}