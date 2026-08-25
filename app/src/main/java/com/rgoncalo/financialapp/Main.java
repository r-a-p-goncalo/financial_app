package com.rgoncalo.financialapp;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.application.ApplicationConfiguration;
import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.bootstrap.BootstrapConfigLoader;
import com.rgoncalo.financialapp.bootstrap.BootstrapRunner;
import com.rgoncalo.financialapp.cli.usercontext.UserContextCli;
import com.rgoncalo.financialapp.client.ClientApplication;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.*;

import java.sql.Connection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final Path DEFAULT_BOOTSTRAP_FILE =
            Path.of("config", "bootstrap.json");

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

        bootstrapConfigurationFrom(args).ifPresent(
                configuration -> runBootstrap(
                        serverApp,
                        configuration
                )
        );

        ClientApplication clientApplication = new ClientApplication(serverApp);

        UserContextCli userContextCli = new UserContextCli(new Scanner(System.in), clientApplication);

        userContextCli.runCliLoop();
    }

    private static Optional<BootstrapConfiguration>
    bootstrapConfigurationFrom(String[] args) {

        for (int index = 0; index < args.length; index++) {
            if ("--no-bootstrap".equals(args[index])) {
                return Optional.empty();
            }

            if ("--bootstrap".equals(args[index])) {
                if (index + 1 == args.length) {
                    throw new IllegalArgumentException(
                            "--bootstrap requires a configuration file path"
                    );
                }

                return Optional.of(
                        new BootstrapConfiguration(
                                Path.of(args[index + 1]),
                                true
                        )
                );
            }
        }

        return Optional.of(
                new BootstrapConfiguration(
                        DEFAULT_BOOTSTRAP_FILE,
                        false
                )
        );
    }

    private static void runBootstrap(
            Application application,
            BootstrapConfiguration configuration
    ) {

        if (!Files.isRegularFile(configuration.file())) {
            if (configuration.required()) {
                throw new IllegalArgumentException(
                        "Bootstrap configuration does not exist: "
                                + configuration.file().toAbsolutePath()
                );
            }

            return;
        }

        new BootstrapRunner(application).run(
                new BootstrapConfigLoader().load(configuration.file())
        );
    }

    private record BootstrapConfiguration(
            Path file,
            boolean required
    ) {
    }
}
