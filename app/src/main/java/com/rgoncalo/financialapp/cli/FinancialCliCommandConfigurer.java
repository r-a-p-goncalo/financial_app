package com.rgoncalo.financialapp.cli;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.cli.commands.AccountsSummaryCommand;
import com.rgoncalo.financialapp.cli.commands.Command;
import com.rgoncalo.financialapp.cli.commands.CreateAccountCommand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FinancialCliCommandConfigurer {

    public static Collection<Command> configureCommands(Application app){
        List<Command> commands = new ArrayList<Command>();

        commands.add( new CreateAccountCommand(app.createAccount()));
        commands.add( new AccountsSummaryCommand(app.accountSummary()));

        return commands;
    }

}
