package com.rgoncalo.financialapp.configuration;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextPermissionRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.application.user.UserRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.jdbc.DatabaseDataSourceFactory;
import com.rgoncalo.financialapp.infrastructure.persistence.jdbc.DatabaseDialect;
import com.rgoncalo.financialapp.infrastructure.persistence.jdbc.DatabaseMigrator;
import com.rgoncalo.financialapp.infrastructure.persistence.jdbc.JdbcAccountRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.jdbc.JdbcFinancialContextPermissionRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.jdbc.JdbcFinancialContextRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.jdbc.JdbcTransactionRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.jdbc.JdbcUserRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * Runs the repository contract against a disposable PostgreSQL database.
 *
 * <p>The container is local to the developer or CI worker. Each test
 * invocation receives a fresh database, migrated with the same PostgreSQL
 * scripts used by RDS deployments.</p>
 */
public class PostgreSQLTestConfiguration
        implements RepositoryTestConfiguration {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("financial_app_tests")
                    .withUsername("test_user")
                    .withPassword("test_password");

    private final String databaseName;
    private final DataSource dataSource;

    public PostgreSQLTestConfiguration() {
        startContainer();

        databaseName = "financial_app_test_"
                + UUID.randomUUID().toString().replace("-", "");
        createDatabase(databaseName);

        dataSource = DatabaseDataSourceFactory.create(
                DatabaseDialect.POSTGRESQL,
                jdbcUrl(databaseName),
                POSTGRES.getUsername(),
                POSTGRES.getPassword(),
                2
        );
        DatabaseMigrator.migrate(dataSource, DatabaseDialect.POSTGRESQL);
    }

    @Override
    public String name() {
        return "PostgreSQL";
    }

    @Override
    public AccountRepository createAccountRepository() {
        return new JdbcAccountRepository(dataSource);
    }

    @Override
    public FinancialContextRepository createFinancialContextRepository() {
        return new JdbcFinancialContextRepository(dataSource);
    }

    @Override
    public TransactionRepository createTransactionRepository() {
        return new JdbcTransactionRepository(dataSource);
    }

    @Override
    public UserRepository createUserRepository() {
        return new JdbcUserRepository(dataSource);
    }

    @Override
    public FinancialContextPermissionRepository
    createFinancialContextPermissionRepository() {
        return new JdbcFinancialContextPermissionRepository(dataSource);
    }

    @Override
    public void close() throws Exception {
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            hikariDataSource.close();
        }

        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword()
        ); Statement statement = connection.createStatement()) {
            statement.execute("DROP DATABASE IF EXISTS " + databaseName
                    + " WITH (FORCE)");
        }
    }

    private static synchronized void startContainer() {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
    }

    private static void createDatabase(String databaseName) {
        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword()
        ); Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE " + databaseName);
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Could not create PostgreSQL test database.",
                    exception
            );
        }
    }

    private static String jdbcUrl(String databaseName) {
        return "jdbc:postgresql://" + POSTGRES.getHost() + ":"
                + POSTGRES.getMappedPort(5432) + "/" + databaseName;
    }
}
