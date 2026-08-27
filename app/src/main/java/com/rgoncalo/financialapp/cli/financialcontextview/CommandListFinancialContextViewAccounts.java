package com.rgoncalo.financialapp.cli.financialcontextview;

import com.rgoncalo.financialapp.cli.CLICommand;
import com.rgoncalo.financialapp.client.ClientApplication;
import com.rgoncalo.financialapp.client.data.account.ClientAccount;
import com.rgoncalo.financialapp.client.data.financialcontext.FinancialContextView;

import java.util.Scanner;

/**
 * Displays every account's total in the currently loaded dated view.
 */
public class CommandListFinancialContextViewAccounts extends CLICommand {

    public CommandListFinancialContextViewAccounts(ClientApplication app) {
        super(app);
    }

    @Override
    public String commandString() {
        return "list-accounts";
    }

    @Override
    public void execute(Scanner scanner) {
        FinancialContextView view = app.getCurrentFinancialContextView();

        System.out.println();
        System.out.println("Accounts as of " + view.date() + ":");

        for (ClientAccount account : view.accounts()) {
            System.out.println("---");
            System.out.println("    Id: " + account.account().accountRecordId());
            System.out.println("    Name: " + account.account().name());
            System.out.println("    Current total: " + account.currentTotal());
        }

        System.out.println();
    }

    @Override
    public String help() {
        return "lists account totals in the loaded financial context view";
    }
}
