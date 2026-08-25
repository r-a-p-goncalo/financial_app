package com.rgoncalo.financialapp.cli.account;

import com.rgoncalo.financialapp.cli.CLI;
import com.rgoncalo.financialapp.cli.CliCommandInter;
import com.rgoncalo.financialapp.client.ClientApplication;

import java.util.Collection;
import java.util.List;
import java.util.Scanner;

/**
 * CLI commands that operate on the account currently loaded by the client.
 */
public class AccountCli extends CLI {

    public AccountCli(Scanner scanner, ClientApplication app) {
        super(scanner, app);
    }

    @Override
    public Collection<CliCommandInter> configureCommands(
            ClientApplication app
    ) {

        return List.of(
                new CommandListAccountTransactionSummaries(app)
        );
    }
}
