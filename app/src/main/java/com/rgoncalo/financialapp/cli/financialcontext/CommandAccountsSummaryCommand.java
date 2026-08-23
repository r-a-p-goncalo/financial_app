package com.rgoncalo.financialapp.cli.financialcontext;

import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.cli.CLICommand;
import com.rgoncalo.financialapp.client.ClientApplication;

import java.util.Collection;
import java.util.Scanner;

import static com.rgoncalo.financialapp.utils.StringUtils.printIfNotNull;

public class CommandAccountsSummaryCommand extends CLICommand {

    public CommandAccountsSummaryCommand(ClientApplication app) {
        super(app);
    }

    @Override
    public String commandString(){
        return "list-accounts";
    }


    @Override
    public void execute(Scanner scanner) {

        System.out.println("Accounts:");

        Collection<AccountRecord> accounts = this.app.listAccountsSummary();

        for(AccountRecord account : accounts){
            System.out.println("---");
            printIfNotNull("    Id: ", account.accountRecordId());
            printIfNotNull("    Name: ", account.name());
            printIfNotNull("    Initial amount: ", account.initialAmount());
        }

        System.out.println();

    }

    @Override
    public String help() {
        return "gets a summary of current accounts";
    }
}