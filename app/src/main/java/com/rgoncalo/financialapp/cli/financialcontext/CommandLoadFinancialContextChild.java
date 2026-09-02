package com.rgoncalo.financialapp.cli.financialcontext;

import com.rgoncalo.financialapp.cli.CLICommand;
import com.rgoncalo.financialapp.client.ClientApplication;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextId;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;

import java.util.Scanner;

public class CommandLoadFinancialContextChild extends CLICommand {

    public CommandLoadFinancialContextChild(ClientApplication app) {
        super(app);
    }

    @Override
    public void execute(Scanner scanner) {
        System.out.println("Loading a child financial context.");
        System.out.print("Child financial context id or name: ");
        String idOrName = scanner.nextLine().trim();
        FinancialContextId childId = app.getCurrentFinancialContextChildren()
                .stream()
                .filter(child -> matches(child, idOrName))
                .map(FinancialContextRecord::financialContextId)
                .findFirst()
                .orElseGet(() -> new FinancialContextId(idOrName));

        app.loadCurrentFinancialContextChild(childId);

        FinancialContextRecord child = app.getCurrentFinancialContext();
        System.out.println();
        System.out.println("Loaded child financial context with name "
                + child.name() + " and id " + child.financialContextId());
        System.out.println();
    }

    private boolean matches(
            FinancialContextRecord child,
            String idOrName
    ) {
        return child.financialContextId().financialContextId().equals(idOrName)
                || child.name().equals(idOrName);
    }

    @Override
    public String commandString() {
        return "load-child";
    }

    @Override
    public String help() {
        return "loads a direct child of the current context";
    }
}
