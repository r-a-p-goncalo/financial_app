package com.rgoncalo.financialapp.cli.usercontext;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.cli.CLI;
import com.rgoncalo.financialapp.cli.CliCommandInter;
import com.rgoncalo.financialapp.client.ClientApplication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

/**
 * This is a CLI directly available to the user, from where he can select financial contexts and so on
 */
public class UserContextCli extends CLI {


    public UserContextCli(Scanner scanner, ClientApplication app) {
        super(scanner, app);
    }

    @Override
    public Collection<CliCommandInter> configureCommands(ClientApplication app){

        List<CliCommandInter> commands = new ArrayList<CliCommandInter>();

        commands.add(new CommandListFinancialContextSummary(app));
        commands.add(new CommandCreateFinancialContext(app));
        commands.add(new CommandLoadFinancialContext(app));

        return commands;
    }

}
