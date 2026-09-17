package com.rgoncalo.financialapp;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.application.ApplicationConfiguration;
import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextPermissionRepository;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.application.user.UserRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.*;
import com.rgoncalo.financialapp.infrastructure.security.PasswordHashingStrategyRegistry;
import com.rgoncalo.financialapp.infrastructure.security.Pbkdf2PasswordHashingStrategy;
import com.rgoncalo.financialapp.logging.ApplicationLogging;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.sql.Connection;
import java.nio.file.Path;

@SpringBootApplication
public class Main {

    private static final Path DEFAULT_DATABASE_FILE =
            Path.of("data", "financial-app.db");

    public static ApplicationConfiguration configureApplication(){

        configureLogging();

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
        configureLogging();
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public Application financialApplication() {
        return createApplication(configureApplication());
    }

    private static void configureLogging() {
        if (System.getProperty(ApplicationLogging.LOG_FILE_PROPERTY) == null) {
            ApplicationLogging.configureForDatabase(DEFAULT_DATABASE_FILE);
        }
    }
}
