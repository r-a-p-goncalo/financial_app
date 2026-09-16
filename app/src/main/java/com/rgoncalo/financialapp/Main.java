package com.rgoncalo.financialapp;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.application.ApplicationConfiguration;
import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextPermissionRepository;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.application.user.UserRepository;
import com.rgoncalo.financialapp.bootstrap.BootstrapConfigLoader;
import com.rgoncalo.financialapp.bootstrap.BootstrapRunner;
import com.rgoncalo.financialapp.cli.usercontext.UserContextCli;
import com.rgoncalo.financialapp.client.ClientApplication;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.*;
import com.rgoncalo.financialapp.logging.ApplicationLogging;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermission;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextPermissionRecord;
import com.rgoncalo.financialapp.commondata.user.UserId;
import com.rgoncalo.financialapp.commondata.user.UserRecord;

import java.sql.Connection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final UserId LOCAL_DEVELOPMENT_USER_ID =
            new UserId("local-development-user");

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

        provisionLocalDevelopmentUser(
                userRepository,
                financialContextRepository,
                permissionRepository
        );

        return new ApplicationConfiguration(
                accountRepository,
                financialContextRepository,
                transactionRepository,
                userRepository,
                permissionRepository
        );

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
                        configuration,
                        LOCAL_DEVELOPMENT_USER_ID
                )
        );

        ClientApplication clientApplication = new ClientApplication(
                serverApp,
                LOCAL_DEVELOPMENT_USER_ID
        );

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
            BootstrapConfiguration configuration,
            UserId userId
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

        new BootstrapRunner(application, userId).run(
                new BootstrapConfigLoader().load(configuration.file())
        );
    }

    private static void provisionLocalDevelopmentUser(
            UserRepository userRepository,
            FinancialContextRepository financialContextRepository,
            FinancialContextPermissionRepository permissionRepository
    ) {
        userRepository.findById(LOCAL_DEVELOPMENT_USER_ID).orElseGet(() ->
                userRepository.save(new UserRecord(
                        LOCAL_DEVELOPMENT_USER_ID,
                        "Local development user"
                ))
        );

        financialContextRepository.listFinancialContextsSummary()
                .forEach(context -> {
                    if (permissionRepository.listByFinancialContextId(
                            context.financialContextId()
                    ).isEmpty()) {
                        permissionRepository.save(
                                new FinancialContextPermissionRecord(
                                        context.financialContextId(),
                                        LOCAL_DEVELOPMENT_USER_ID,
                                        FinancialContextPermission.OWNER,
                                        LOCAL_DEVELOPMENT_USER_ID,
                                        java.time.Instant.now()
                                )
                        );
                    }
                });
    }

    private record BootstrapConfiguration(
            Path file,
            boolean required
    ) {
    }
}
