package com.rgoncalo.financialapp.cli.usercontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.cli.CLICommand;
import com.rgoncalo.financialapp.cli.financialcontext.FinancialCli;
import com.rgoncalo.financialapp.client.ClientApplication;

import java.util.Scanner;

public class CommandLoadFinancialContext extends CLICommand {

    public CommandLoadFinancialContext(ClientApplication app) {
        super(app);
    }

    @Override
    public void execute(Scanner scanner) {

        System.out.println("Loading into a financial context.");

        System.out.print("Financial context id: ");
        String id = scanner.nextLine().trim();

        this.app.loadIntoFinancialContext(app.getFinancialContextIdFrom(id));

        FinancialContextRecord loadedFinancialContext = this.app.getCurrentFinancialContext();

        System.out.println();
        System.out.println("Loaded financial context with name " + loadedFinancialContext.name() + " and id " + loadedFinancialContext.financialContextId());

        FinancialCli financialCli = new FinancialCli(scanner, this.app);
        financialCli.runCliLoop();

        System.out.println();
        System.out.println("Left financial context cli of id " + id);
        System.out.println();

    }

    @Override
    public String commandString() {
        return "load";
    }

    @Override
    public String help() {
        return "Loads into a specific financial context";
    }
}