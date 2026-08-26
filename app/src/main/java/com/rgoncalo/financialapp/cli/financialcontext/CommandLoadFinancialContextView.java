package com.rgoncalo.financialapp.cli.financialcontext;

import com.rgoncalo.financialapp.cli.CLICommand;
import com.rgoncalo.financialapp.cli.financialcontextview.FinancialContextViewCli;
import com.rgoncalo.financialapp.client.ClientApplication;
import com.rgoncalo.financialapp.client.ClientRuntimeException;
import com.rgoncalo.financialapp.client.data.financialcontext.FinancialContextView;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Loads a dated calculated view of the financial context currently open.
 */
public class CommandLoadFinancialContextView extends CLICommand {

    public CommandLoadFinancialContextView(ClientApplication app) {
        super(app);
    }

    @Override
    public String commandString() {
        return "load-view";
    }

    @Override
    public void execute(Scanner scanner) {

        System.out.println("Loading financial context view.");
        System.out.print("View date (YYYY-MM-DD, blank for current date): ");
        String dateInput = scanner.nextLine().trim();

        FinancialContextView view;

        if (dateInput.isEmpty()) {
            view = app.loadFinancialContextView();
        } else {
            try {
                view = app.loadFinancialContextView(
                        LocalDate.parse(dateInput)
                );
            } catch (DateTimeParseException exception) {
                throw new ClientRuntimeException(
                        "View date must be in YYYY-MM-DD format"
                );
            }
        }

        System.out.println();
        System.out.println(
                "Loaded financial context view for " + view.date()
        );

        try {
            new FinancialContextViewCli(scanner, app).runCliLoop();
        } finally {
            app.unloadFinancialContextView();
        }

        System.out.println();
        System.out.println("Left financial context view for " + view.date());
        System.out.println();
    }

    @Override
    public String help() {
        return "loads a dated financial context view";
    }
}
