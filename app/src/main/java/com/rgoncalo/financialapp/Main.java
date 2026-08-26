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
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.*;

import java.sql.Connection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final Path DEFAULT_BOOTSTRAP_FILE =
            Path.of("config", "bootstrap.json");

    public static ApplicationConfiguration configureApplication(){

        SQLiteConnection sqliteConnection =
                new SQLiteConnection(
                        "data/financial-app.db"
                );


        Connection connection =
                sqliteConnection.getConnection();

        SQLiteSchema.initialize(connection);

        AccountRepository accountRepository = new SQLiteAccountRepository(
                connection
        );

        FinancialContextRepository financialContextRepository = new SQLiteFinancialContextRepository(
                connection
        );

        TransactionRepository transactionRepository = new SQLiteTransactionRepository(
                connection
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

        System.out.println("Running bootstrap configuration required: " + configuration.required + ", file: " + configuration.file);

        if (!Files.isRegularFile(configuration.file())) {
            if (configuration.required()) {
                throw new IllegalArgumentException(
                        "Bootstrap configuration does not exist: "
                                + configuration.file().toAbsolutePath()
                );
            }

            System.out.println("Bootstrap configuration was not found and was not required, in path " + configuration.file());

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
