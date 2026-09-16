package com.rgoncalo.financialapp.cli;

import com.rgoncalo.financialapp.client.ClientApplication;
import com.rgoncalo.financialapp.client.ClientRuntimeException;
import com.rgoncalo.financialapp.application.security.AccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class CLI implements  CLIInterface {

    private final Scanner scanner;

    private final HashMap<String, CliCommandInter> commands;

    private static final Logger logger =
            LoggerFactory.getLogger(CLI.class);

    public CLI(
            Scanner scanner,
            ClientApplication app
    ) {
        this.scanner = scanner;

        this.commands = new HashMap<String, CliCommandInter>();

        for(CliCommandInter command : this.configureCommands(app))
            this.commands.put(command.commandString(), command);

    }



    public void runCliLoop() {
        boolean toRun = true;

        System.out.println("Type 'help' to see available commands.");

        while (toRun) {
            System.out.print("> ");

            String commandString = scanner.nextLine().trim();

            switch (commandString) {

                case "help" -> printHelp();

                case "exit" -> toRun = false;

                case "" -> {}

                default ->{

                    CliCommandInter command = this.commands.get(commandString);

                    if (command == null) {
                        System.out.println("Unknown command: " + commandString);
                    }
                    else{
                        try {
                            logger.info("Executing command of CLI {}: {}\n", this.getClass().getSimpleName(), commandString);
                            command.execute(scanner);
                        } catch (ClientRuntimeException | AccessDeniedException e){
                            logger.error("Error when executing command of CLI {}: {}\n", this.getClass().getSimpleName(), e.getMessage(), e);
                            System.out.println("Error when executing command: " + e.getMessage());
                        }
                    }
                }
            }
        }

        System.out.println("Goodbye.");
    }

    private void printHelp() {
        System.out.println();
        System.out.println("Available commands:");
        System.out.println("  help            Show available commands");
        System.out.println("  exit            Exit the application");

        for(String commandString : this.commands.keySet()){
            CliCommandInter command = this.commands.get(commandString);
            System.out.println("  " + commandString + "\n                  " + command.help());
        }

        System.out.println();
    }

}
