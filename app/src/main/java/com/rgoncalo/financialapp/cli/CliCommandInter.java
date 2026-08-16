package com.rgoncalo.financialapp.cli;

import java.util.Scanner;

public interface CliCommandInter {

    void execute(Scanner scanner);
    String commandString();
    String help();

}
