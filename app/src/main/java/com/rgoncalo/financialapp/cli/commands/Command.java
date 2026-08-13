package com.rgoncalo.financialapp.cli.commands;

import java.util.Scanner;

public interface Command {

    void execute(Scanner scanner);
    String commandString();
    String help();

}
