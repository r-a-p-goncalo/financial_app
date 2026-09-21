package com.rgoncalo.financialapp;

import com.rgoncalo.financialapp.application.Application;
import com.rgoncalo.financialapp.application.ApplicationConfiguration;
import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextPermissionRepository;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.application.user.UserRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.jdbc.*;
import com.rgoncalo.financialapp.infrastructure.security.PasswordHashingStrategyRegistry;
import com.rgoncalo.financialapp.infrastructure.security.Pbkdf2PasswordHashingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Path;
import javax.sql.DataSource;

@SpringBootApplication
public class Main {

    private static final Path DEFAULT_DATABASE_FILE =
            Path.of("data", "financial-app.db");

    public static ApplicationConfiguration configureApplication() {
        return configureApplication(DEFAULT_DATABASE_FILE);
    }

    public static ApplicationConfiguration configureApplication(Path databaseFile) {
        DataSource dataSource = DatabaseDataSourceFactory.create(
                DatabaseDialect.SQLITE,
                "jdbc:sqlite:" + databaseFile,
                "",
                "",
                1
        );
        return configureApplication(dataSource, DatabaseDialect.SQLITE);
    }

    public static ApplicationConfiguration configureApplication(
            DataSource dataSource,
            DatabaseDialect dialect
    ) {
        DatabaseMigrator.migrate(dataSource, dialect);

        AccountRepository accountRepository = new JdbcAccountRepository(
                dataSource
        );

        FinancialContextRepository financialContextRepository = new JdbcFinancialContextRepository(
                dataSource
        );

        TransactionRepository transactionRepository = new JdbcTransactionRepository(
                dataSource
        );

        UserRepository userRepository = new JdbcUserRepository(dataSource);
        FinancialContextPermissionRepository permissionRepository =
                new JdbcFinancialContextPermissionRepository(dataSource);

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
    public DataSource financialAppDataSource(
            @Value("${financial-app.database.dialect:sqlite}")
            String dialectValue,
            @Value("${financial-app.database.jdbc-url:jdbc:sqlite:data/financial-app.db}")
            String jdbcUrl,
            @Value("${financial-app.database.username:}")
            String username,
            @Value("${financial-app.database.password:}")
            String password,
            @Value("${financial-app.database.maximum-pool-size:10}")
            int maximumPoolSize
    ) {
        return DatabaseDataSourceFactory.create(
                DatabaseDialect.fromConfiguration(dialectValue),
                jdbcUrl,
                username,
                password,
                maximumPoolSize
        );
    }

    @Bean
    public Application financialApplication(
            DataSource financialAppDataSource,
            @Value("${financial-app.database.dialect:sqlite}")
            String dialectValue
    ) {
        return createApplication(configureApplication(
                financialAppDataSource,
                DatabaseDialect.fromConfiguration(dialectValue)
        ));
    }
}
