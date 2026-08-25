package com.rgoncalo.financialapp.cli.financialcontext;

import com.rgoncalo.financialapp.cli.CLICommand;
import com.rgoncalo.financialapp.cli.account.AccountCli;
import com.rgoncalo.financialapp.client.ClientApplication;
import com.rgoncalo.financialapp.commondata.account.AccountRecordId;

import java.util.Scanner;

/**
 * Loads an account and starts an account-specific command loop.
 */
public class CommandLoadAccount extends CLICommand {

    public CommandLoadAccount(ClientApplication app) {
        super(app);
    }

    @Override
    public String commandString() {
        return "load-account";
    }

    @Override
    public void execute(Scanner scanner) {

        System.out.println("Loading account.");
        System.out.print("Account id: ");
        String accountId = scanner.nextLine().trim();

        AccountRecordId accountRecordId = app.getAccountRecordIdIdFrom(
                accountId
        );

        app.loadAccount(accountRecordId);

        System.out.println();
        System.out.println(
                "Loaded account with name "
                        + app.getCurrentAccount().record().name()
                        + " and id "
                        + app.getCurrentAccount().record().accountRecordId()
        );

        try {
            new AccountCli(scanner, app).runCliLoop();
        } finally {
            app.unloadAccount();
        }

        System.out.println();
        System.out.println("Left account cli of id " + accountId);
        System.out.println();
    }

    @Override
    public String help() {
        return "loads a specific account";
    }
}
