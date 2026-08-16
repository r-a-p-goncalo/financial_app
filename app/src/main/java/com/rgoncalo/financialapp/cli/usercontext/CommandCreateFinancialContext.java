package com.rgoncalo.financialapp.cli.usercontext;

import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.cli.CLICommand;
import com.rgoncalo.financialapp.client.ClientApplication;

import java.util.Collection;
import java.util.Scanner;

import static com.rgoncalo.financialapp.utils.StringUtils.printIfNotNull;

public class CommandCreateFinancialContext extends CLICommand {

    public CommandCreateFinancialContext(ClientApplication app) {
        super(app);
    }

    @Override
    public void execute(Scanner scanner) {

        System.out.println("Creating financial context.");

        System.out.print("Financial context name: ");
        String name = scanner.nextLine().trim();

        this.app.createFinancialRecord(name);

        System.out.println();

    }

    @Override
    public String commandString() {
        return "create";
    }

    @Override
    public String help() {
        return "Creates a financial context";
    }
}