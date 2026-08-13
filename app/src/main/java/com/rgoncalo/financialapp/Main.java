package com.rgoncalo.financialapp;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.application.ApplicationConfiguration;
import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.cli.FinancialCli;
import com.rgoncalo.financialapp.infrastructure.database.sqlite.SQLiteAccountRepository;
import com.rgoncalo.financialapp.infrastructure.database.sqlite.SQLiteConnection;
import com.rgoncalo.financialapp.infrastructure.database.sqlite.SQLiteSchema;

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


        return new ApplicationConfiguration(accountRepository);

    }

    public static Application createApplication(ApplicationConfiguration appConfig){
        return new Application(appConfig);
    }

    public static void main(String[] args) {


        ApplicationConfiguration appConfig = configureApplication();
        Application app = createApplication(appConfig);

        FinancialCli cli =
                new FinancialCli(
                        new Scanner(System.in),
                        app
                );

        cli.runCliLoop();
    }
}