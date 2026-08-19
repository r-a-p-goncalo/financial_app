package com.rgoncalo.financialapp;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.application.ApplicationConfiguration;
import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.cli.usercontext.UserContextCli;
import com.rgoncalo.financialapp.client.ClientApplication;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.SQLiteAccountRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.SQLiteConnection;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.SQLiteFinancialContextRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.SQLiteSchema;

import java.sql.Connection;
import java.util.Scanner;

public class Main {

    public static ApplicationConfiguration configureApplication(){

        SQLiteConnection sqliteConnection =
                new SQLiteConnection(
                        "data/financial-app.db"
                );


        Connection connection =
                sqliteConnection.getConnection();

        SQLiteSchema.initialize(connection);

        AccountRepository accountRepository =
                new SQLiteAccountRepository(connection);

        FinancialContextRepository financialContextRepository = new SQLiteFinancialContextRepository(connection);

        return new ApplicationConfiguration(accountRepository, financialContextRepository);

    }

    public static Application createApplication(ApplicationConfiguration appConfig){
        return new Application(appConfig);
    }

    public static void main(String[] args) {


        ApplicationConfiguration serverAppConfig = configureApplication();
        Application serverApp = createApplication(serverAppConfig);

        ClientApplication clientApplication = new ClientApplication(serverApp);

        UserContextCli userContextCli = new UserContextCli(new Scanner(System.in), clientApplication);

        userContextCli.runCliLoop();
    }
}