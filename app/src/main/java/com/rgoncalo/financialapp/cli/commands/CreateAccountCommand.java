package com.rgoncalo.financialapp.cli.commands;

import com.rgoncalo.financialapp.application.account.CreateAccount;
import com.rgoncalo.financialapp.application.account.CreateAccountRequest;
import com.rgoncalo.financialapp.domain.account.Account;
import com.rgoncalo.financialapp.domain.money.MonetaryValue;

import java.util.Scanner;


public class CreateAccountCommand implements Command {

    private static final String COMMAND_STRING = "create_account";

    private final CreateAccount createAccount;

    public CreateAccountCommand(CreateAccount createAccount) {
        this.createAccount = createAccount;
    }

    @Override
    public String commandString(){
        return COMMAND_STRING;
    }

    @Override
    public void execute(Scanner scanner) {

        System.out.println("Creating account.");

        System.out.print("Account name: ");
        String name = scanner.nextLine().trim();

        System.out.println("Currency: \n");

        System.out.print("Initial amount: ");
        String initialAmount = scanner.nextLine().trim();

        try {
            MonetaryValue amount = new MonetaryValue(
                    Double.parseDouble(initialAmount)
            );

            Account account = createAccount.execute(new CreateAccountRequest(name, amount));

            System.out.println();
            System.out.println("Account created.");
            System.out.println("Id: " + account.getId());
            System.out.println("Name: " + account.getName());
            System.out.println("Initial amount: " + account.getInitialAmount());
            System.out.println();

        } catch (IllegalArgumentException exception) {
            System.out.println("Could not create account: "
                    + exception.getMessage());
        }
    }

    @Override
    public String help() {
        return "creates an account";
    }
}