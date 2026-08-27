package com.rgoncalo.financialapp.cli;

import com.rgoncalo.financialapp.client.ClientApplication;

import java.util.Scanner;

public abstract class CLICommand implements CliCommandInter{

    protected static final String UNKNOWN_OR_IRRELEVANT_ACCOUNT = "Unknown or irrelevant account";

    protected final ClientApplication app;

    public CLICommand(ClientApplication app){
        this.app = app;
    }

    @Override
    public String help() {
        return "This is a placeholder help text";
    }
}
