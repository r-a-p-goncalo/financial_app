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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Path;
import java.sql.Connection;

@SpringBootApplication
public class Main {

    private static final Path DEFAULT_DATABASE_FILE =
            Path.of("data", "financial-app.db");

    public static ApplicationConfiguration configureApplication() {
        return configureApplication(DEFAULT_DATABASE_FILE);
    }

    public static ApplicationConfiguration configureApplication(Path databaseFile) {

        SQLiteConnection sqliteConnection =
                new SQLiteConnection(
                        databaseFile.toString()
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

    public static Application createApplication(ApplicationConfiguration appConfig) {
        return new Application(appConfig);
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public Application financialApplication(
            @Value("${financial-app.database.path:data/financial-app.db}")
            String databasePath
    ) {
        return createApplication(configureApplication(Path.of(databasePath)));
    }
}
