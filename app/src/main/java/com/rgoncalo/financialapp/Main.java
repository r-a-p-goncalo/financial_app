package com.rgoncalo.financialapp;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.application.ApplicationConfiguration;
import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.cli.usercontext.UserContextCli;
import com.rgoncalo.financialapp.client.ClientApplication;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.*;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.typeconverter.SQLiteTypeConverters;

import java.sql.Connection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static final String FINANCIAL_CONTEXT_ID_STRING = "financialContextId_financialContextId";
    public static final String ACCOUNT_FINANCIAL_CONTEXT_ID_STRING = "accountRecordId_" + FINANCIAL_CONTEXT_ID_STRING;
    public static final String ACCOUNT_ID_STRING = "accountRecordId_accountRecordId";
    public static final String TRANSACTION_FINANCIAL_CONTEXT_ID_STRING = "transactionRecordId_" + FINANCIAL_CONTEXT_ID_STRING;
    public static final String TRANSACTION_ID_STRING = "transactionRecordId_transactionRecordId";


    public static ApplicationConfiguration configureApplication(){

        SQLiteConnection sqliteConnection =
                new SQLiteConnection(
                        "data/financial-app.db"
                );


        Connection connection =
                sqliteConnection.getConnection();

        SQLiteSchema.initialize(
                connection,
                new AbstractMap.SimpleEntry<>(FinancialContextRecord.class, List.of(FINANCIAL_CONTEXT_ID_STRING)),
                new AbstractMap.SimpleEntry<>(AccountRecord.class, List.of(ACCOUNT_FINANCIAL_CONTEXT_ID_STRING, ACCOUNT_ID_STRING)),
                new AbstractMap.SimpleEntry<>(TransactionRecord.class, List.of(TRANSACTION_FINANCIAL_CONTEXT_ID_STRING, TRANSACTION_ID_STRING))
        );

        AccountRepository accountRepository = new SQLiteAccountRepository(
                new SQLiteRepositoryFactory<AccountRecord>().sqLiteRepositoryOfType(connection, AccountRecord.class)
        );

        FinancialContextRepository financialContextRepository = new SQLiteFinancialContextRepository(
                new SQLiteRepositoryFactory<FinancialContextRecord>().sqLiteRepositoryOfType(connection, FinancialContextRecord.class)
        );

        TransactionRepository transactionRepository = new SQLiteTransactionRepository(
                new SQLiteRepositoryFactory<TransactionRecord>().sqLiteRepositoryOfType(connection, TransactionRecord.class)
        );

        return new ApplicationConfiguration(accountRepository, financialContextRepository, transactionRepository);

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