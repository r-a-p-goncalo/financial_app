package com.rgoncalo.financialapp.cli.financialcontext;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.cli.CLICommand;
import com.rgoncalo.financialapp.client.ClientApplication;
import com.rgoncalo.financialapp.commondata.money.MonetaryValue;

import java.util.Scanner;


public class CommandCreateAccountCommand extends CLICommand {


    public CommandCreateAccountCommand(ClientApplication app) {
        super(app);
    }
    @Override
    public String commandString(){
        return "create-account";
    }

    @Override
    public void execute(Scanner scanner) {

        System.out.println("Creating account.");

        System.out.print("Account name: ");
        String name = scanner.nextLine().trim();

        System.out.println("Currency: \n");

        System.out.print("Initial amount: ");
        String initialAmount = scanner.nextLine().trim();

        MonetaryValue amount = new MonetaryValue(
                Double.parseDouble(initialAmount)
        );

        AccountRecord accountRecord = this.app.createAccount(name, amount);

        System.out.println();
        System.out.println("Account created.");
        System.out.println("Id: " + accountRecord.id());
        System.out.println("Name: " + accountRecord.name());
        System.out.println("Initial amount: " + accountRecord.initialAmount());
        System.out.println();

    }

    @Override
    public String help() {
        return "creates an account";
    }
}