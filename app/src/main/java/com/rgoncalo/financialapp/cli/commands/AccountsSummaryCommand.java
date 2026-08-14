package com.rgoncalo.financialapp.cli.commands;

import com.rgoncalo.financialapp.application.account.AccountsSummary;
import com.rgoncalo.financialapp.application.account.AccountsSummaryRequest;
import com.rgoncalo.financialapp.domain.account.Account;

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

            Collection<Account> accounts = accountsSummaryApp.execute(new AccountsSummaryRequest());

            for(Account account : accounts){
                System.out.println("---");
                printIfNotNull("    Id: ", account.getId());
                printIfNotNull("    Name: ", account.getName());
                printIfNotNull("    Initial amount: ", account.getInitialAmount());
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