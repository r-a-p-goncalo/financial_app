package com.rgoncalo.financialapp.cli.commands;

import com.rgoncalo.financialapp.application.account.AccountRecord;
import com.rgoncalo.financialapp.application.account.AccountsSummary;
import com.rgoncalo.financialapp.application.account.AccountsSummaryRequest;

import java.util.Collection;
import java.util.Scanner;

public class AccountsSummaryCommand implements Command {

    private static final String COMMAND_STRING = "accounts_summary";

    private final AccountsSummary accountsSummaryApp;

    public AccountsSummaryCommand(AccountsSummary accountsSummaryApp) {
        this.accountsSummaryApp = accountsSummaryApp;
    }

    @Override
    public String commandString(){
        return COMMAND_STRING;
    }

    public void printIfNotNull(String prefix, Object toPrint){

        if(toPrint != null)
            System.out.println(prefix + toPrint.toString());

    }

    @Override
    public void execute(Scanner scanner) {

        System.out.println("Accounts:");

        try {

            Collection<AccountRecord> accounts = accountsSummaryApp.execute(new AccountsSummaryRequest());

            for(AccountRecord account : accounts){
                System.out.println("---");
                printIfNotNull("    Id: ", account.id());
                printIfNotNull("    Name: ", account.name());
                printIfNotNull("    Initial amount: ", account.initial_value());
            }

            System.out.println();

        } catch (IllegalArgumentException exception) {
            System.out.println("Could not query accounts: "
                    + exception.getMessage());
        }
    }

    @Override
    public String help() {
        return "gets a summary of current accounts";
    }
}