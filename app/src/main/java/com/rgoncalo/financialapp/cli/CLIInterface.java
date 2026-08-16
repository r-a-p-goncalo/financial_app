package com.rgoncalo.financialapp.cli;

import com.rgoncalo.financialapp.client.ClientApplication;

import java.util.Collection;

public interface CLIInterface {

    void runCliLoop();
    Collection<CliCommandInter> configureCommands(ClientApplication app);
}
