package com.rgoncalo.financialapp.cli;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.cli.commands.Command;

import java.util.*;

public class FinancialCli {

    private final Scanner scanner;

    private final HashMap<String, Command> commands;

    public FinancialCli(
            Scanner scanner,
            Application app
    ) {
        this.scanner = scanner;

        this.commands = new HashMap<String, Command>();

        for(Command command : FinancialCliCommandConfigurer.configureCommands(app))
            this.commands.put(command.commandString(), command);

    }

    public void runCliLoop() {
        boolean toRun = true;

        System.out.println("Financial App");
        System.out.println("Type 'help' to see available commands.");

        while (toRun) {
            System.out.print("> ");

            String commandString = scanner.nextLine().trim();

            switch (commandString) {

                case "help" -> printHelp();

                case "exit" -> toRun = false;

                case "" -> {}

                default ->{

                        Command  command = this.commands.get(commandString);

                        if (command == null) {
                            System.out.println("Unknown command: " + command);
                        }
                        else{
                            command.execute(scanner);
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
            Command command = this.commands.get(commandString);
            System.out.println("  " + commandString + "\n                  " + command.help());
        }

        System.out.println();
    }

}