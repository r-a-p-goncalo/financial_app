package com.rgoncalo.financialapp.cli.financialcontextview;

import com.rgoncalo.financialapp.cli.CLICommand;
import com.rgoncalo.financialapp.client.ClientApplication;
import com.rgoncalo.financialapp.client.ClientRuntimeException;
import com.rgoncalo.financialapp.client.data.account.AccountTransactionSummary;
import com.rgoncalo.financialapp.client.data.account.ClientAccount;
import com.rgoncalo.financialapp.client.data.financialcontext.FinancialContextView;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;

import java.util.Scanner;

/**
 * Displays the selected account's dated transaction history and the total
 * left after each transaction.
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

        System.out.print("Account id or name: ");
        String accountIdOrName = scanner.nextLine().trim();

        ClientAccount account = view.findAccount(accountIdOrName)
                .orElseThrow(() -> new ClientRuntimeException(
                        "No account in the financial context view matches: "
                                + accountIdOrName
                ));

        System.out.println();
        System.out.println(
                "Transactions for account: " + account.account().name()
        );

        if (account.transactionSummaries().isEmpty()) {
            System.out.println("No transactions found.");
        }

        for (AccountTransactionSummary summary
                : account.transactionSummaries()) {
            TransactionRecord transaction = summary.transaction();

            System.out.println();
            System.out.println(
                    "Id: " + transaction.transactionRecordId()
                            .transactionRecordId()
            );
            System.out.println("Date: " + transaction.dateTime());
            System.out.println(
                    "Origin: " + accountName(
                            view,
                            transaction.originAccountId()
                    )
            );
            System.out.println(
                    "Target: " + accountName(
                            view,
                            transaction.targetAccountId()
                    )
            );
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
        return "lists an account's transactions with running totals";
    }

    private String accountName(
            FinancialContextView view,
            AccountRecordId accountId
    ) {
        if (accountId == null) {
            return "External";
        }

        return view.findAccount(accountId)
                .map(account -> account.account().name())
                .orElse(accountId.toString());
    }
}
