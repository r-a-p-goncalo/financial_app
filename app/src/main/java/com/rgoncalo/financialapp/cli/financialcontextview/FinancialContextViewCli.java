package com.rgoncalo.financialapp.cli.financialcontextview;

import com.rgoncalo.financialapp.cli.CLI;
import com.rgoncalo.financialapp.cli.CliCommandInter;
import com.rgoncalo.financialapp.client.ClientApplication;

import java.util.Collection;
import java.util.List;
import java.util.Scanner;

/**
 * Commands that present computed data from the currently loaded financial
 * context view.
 */
public class FinancialContextViewCli extends CLI {

    public FinancialContextViewCli(Scanner scanner, ClientApplication app) {
        super(scanner, app);
    }

    @Override
    public Collection<CliCommandInter> configureCommands(ClientApplication app) {
        return List.of(
                new CommandListFinancialContextViewAccounts(app),
                new CommandListFinancialContextViewAccountTransactions(app)
        );
    }
}
