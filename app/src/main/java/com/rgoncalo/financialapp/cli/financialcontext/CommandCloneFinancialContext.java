package com.rgoncalo.financialapp.cli.financialcontext;

import com.rgoncalo.financialapp.cli.CLICommand;
import com.rgoncalo.financialapp.client.ClientApplication;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;

import java.util.Scanner;

public class CommandCloneFinancialContext extends CLICommand {

    public CommandCloneFinancialContext(ClientApplication app) {
        super(app);
    }

    @Override
    public void execute(Scanner scanner) {
        System.out.println("Creating a lazy financial context clone.");
        System.out.print("Child name (blank to inherit the current name): ");
        String name = scanner.nextLine().trim();
        FinancialContextRecord clone = app.cloneCurrentFinancialContext(
                name.isEmpty() ? null : name
        );

        System.out.println();
        System.out.println("Created child financial context.");
        System.out.println("Id: " + clone.financialContextId());
        System.out.println("Name: " + clone.name());
        System.out.println();
    }

    @Override
    public String commandString() {
        return "clone";
    }

    @Override
    public String help() {
        return "creates a lazy child clone of the current context";
    }
}
