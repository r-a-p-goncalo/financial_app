package com.rgoncalo.financialapp.cli.financialcontext;

import com.rgoncalo.financialapp.cli.CLICommand;
import com.rgoncalo.financialapp.client.ClientApplication;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;

import java.util.Collection;
import java.util.Scanner;

public class CommandListFinancialContextChildren extends CLICommand {

    public CommandListFinancialContextChildren(ClientApplication app) {
        super(app);
    }

    @Override
    public void execute(Scanner scanner) {
        Collection<FinancialContextRecord> children = app
                .getCurrentFinancialContextChildren();

        System.out.println("Child financial contexts:");

        if (children.isEmpty()) {
            System.out.println("No child financial contexts found.");
        }

        for (FinancialContextRecord child : children) {
            System.out.println("---");
            System.out.println("    Id: " + child.financialContextId());
            System.out.println("    Name: " + child.name());
        }

        System.out.println();
    }

    @Override
    public String commandString() {
        return "list-children";
    }

    @Override
    public String help() {
        return "lists lazy clones made from the current context";
    }
}
