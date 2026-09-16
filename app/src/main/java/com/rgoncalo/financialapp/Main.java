package com.rgoncalo.financialapp;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.application.ApplicationConfiguration;
import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextPermissionRepository;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.application.user.UserRepository;
import com.rgoncalo.financialapp.bootstrap.BootstrapConfigLoader;
import com.rgoncalo.financialapp.bootstrap.BootstrapPlan;
import com.rgoncalo.financialapp.cli.user.UserAuthenticationCli;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.*;
import com.rgoncalo.financialapp.infrastructure.security.PasswordHashingStrategyRegistry;
import com.rgoncalo.financialapp.infrastructure.security.Pbkdf2PasswordHashingStrategy;
import com.rgoncalo.financialapp.logging.ApplicationLogging;

import java.sql.Connection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final Path DEFAULT_BOOTSTRAP_FILE =
            Path.of("config", "bootstrap.json");

    private static final Path DEFAULT_DATABASE_FILE =
            Path.of("data", "financial-app.db");

    public static ApplicationConfiguration configureApplication(){

        ApplicationLogging.configureForDatabase(DEFAULT_DATABASE_FILE);

        SQLiteConnection sqliteConnection =
                new SQLiteConnection(
                        DEFAULT_DATABASE_FILE.toString()
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

        UserRepository userRepository = new SQLiteUserRepository(connection);
        FinancialContextPermissionRepository permissionRepository =
                new SQLiteFinancialContextPermissionRepository(connection);

        return new ApplicationConfiguration(
                accountRepository,
                financialContextRepository,
                transactionRepository,
                userRepository,
                permissionRepository,
                new PasswordHashingStrategyRegistry(
                        new Pbkdf2PasswordHashingStrategy()
                )
        );

    }

    public static Application createApplication(ApplicationConfiguration appConfig){
        return new Application(appConfig);
    }

    public static void main(String[] args) {

        ApplicationConfiguration serverAppConfig = configureApplication();
        Application serverApp = createApplication(serverAppConfig);

        new UserAuthenticationCli(
                new Scanner(System.in),
                serverApp,
                bootstrapPlanFrom(args)
        ).runCliLoop();
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

    private static Optional<BootstrapPlan> bootstrapPlanFrom(String[] args) {
        Optional<BootstrapConfiguration> configuration =
                bootstrapConfigurationFrom(args);

        if (configuration.isEmpty()) {
            return Optional.empty();
        }

        BootstrapConfiguration bootstrapConfiguration = configuration.get();

        if (!Files.isRegularFile(bootstrapConfiguration.file())) {
            if (bootstrapConfiguration.required()) {
                throw new IllegalArgumentException(
                        "Bootstrap configuration does not exist: "
                                + bootstrapConfiguration.file().toAbsolutePath()
                );
            }

            System.out.println("Bootstrap configuration was not found and was not required, in path " + bootstrapConfiguration.file());
            return Optional.empty();
        }

        return Optional.of(
                new BootstrapConfigLoader().load(bootstrapConfiguration.file())
        );
    }

    private record BootstrapConfiguration(
            Path file,
            boolean required
    ) {
    }
}
