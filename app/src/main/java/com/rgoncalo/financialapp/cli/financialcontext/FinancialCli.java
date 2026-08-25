package com.rgoncalo.financialapp.cli.financialcontext;

import com.rgoncalo.financialapp.cli.CLI;
import com.rgoncalo.financialapp.cli.CliCommandInter;
import com.rgoncalo.financialapp.cli.transaction.CommandCreateTransactionCommand;
import com.rgoncalo.financialapp.cli.transaction.CommandListTransactionSummaries;
import com.rgoncalo.financialapp.cli.usercontext.CommandCreateFinancialContext;
import com.rgoncalo.financialapp.cli.usercontext.CommandListFinancialContextSummary;
import com.rgoncalo.financialapp.client.ClientApplication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

public class FinancialCli extends CLI {


    public FinancialCli(Scanner scanner, ClientApplication app) {
        super(scanner, app);
    }

    @Override
    public Collection<CliCommandInter> configureCommands(ClientApplication app) {

        List<CliCommandInter> commands = new ArrayList<CliCommandInter>();

        commands.add(new CommandAccountsSummaryCommand(app));
        commands.add(new CommandCreateAccountCommand(app));
        commands.add(new CommandCreateTransactionCommand(app));
        commands.add(new CommandListTransactionSummaries(app));
        commands.add(new CommandLoadAccount(app));

        return commands;
    }
}
