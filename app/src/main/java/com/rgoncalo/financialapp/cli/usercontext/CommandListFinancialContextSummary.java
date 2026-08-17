package com.rgoncalo.financialapp.cli.usercontext;

import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.cli.CLICommand;
import com.rgoncalo.financialapp.client.ClientApplication;

import java.util.Collection;
import java.util.Scanner;

import static com.rgoncalo.financialapp.utils.StringUtils.printIfNotNull;

public class CommandListFinancialContextSummary extends CLICommand {

    public CommandListFinancialContextSummary(ClientApplication app) {
        super(app);
    }

    @Override
    public void execute(Scanner scanner) {
        Collection<FinancialContextRecord> financialContextRecords = this.app.getFinancialContexts();

        System.out.println("Financial accounts:");

        for(FinancialContextRecord financialContextRecord : financialContextRecords){
                System.out.println("---");
                printIfNotNull("    Id: ", financialContextRecord.id());
                printIfNotNull("    Name: ", financialContextRecord.name());
        }

        System.out.println();

    }

    @Override
    public String commandString() {
        return "list";
    }

    @Override
    public String help() {
        return "Shows the list of financial contexts";
    }
}
