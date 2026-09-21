package com.rgoncalo.financialapp.configuration;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.application.user.UserRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextPermissionRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.jdbc.DatabaseDataSourceFactory;
import com.rgoncalo.financialapp.infrastructure.persistence.jdbc.DatabaseDialect;
import com.rgoncalo.financialapp.infrastructure.persistence.jdbc.DatabaseMigrator;
import com.rgoncalo.financialapp.infrastructure.persistence.jdbc.JdbcAccountRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.jdbc.JdbcFinancialContextPermissionRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.jdbc.JdbcFinancialContextRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.jdbc.JdbcTransactionRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.jdbc.JdbcUserRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class SQLiteTestConfiguration
        implements RepositoryTestConfiguration {

    private final Connection connection;
    private final DataSource dataSource;

    public SQLiteTestConfiguration() {
        String url = "jdbc:sqlite:file:repository-test-"
                + UUID.randomUUID()
                + "?mode=memory&cache=shared";
        dataSource = DatabaseDataSourceFactory.create(
                DatabaseDialect.SQLITE,
                url,
                "",
                "",
                1
        );

        try {
            // Keep one connection open so the named in-memory database exists
            // for the short-lived repository connections used by each query.
            connection = dataSource.getConnection();
            DatabaseMigrator.migrate(dataSource, DatabaseDialect.SQLITE);
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Could not create SQLite test database.",
                    exception
            );
        }
    }

    @Override
    public String name() {
        return "SQLite";
    }

    @Override
    public AccountRepository createAccountRepository() {

        return new JdbcAccountRepository(dataSource);
    }

    @Override
    public FinancialContextRepository
    createFinancialContextRepository() {

        return new JdbcFinancialContextRepository(dataSource);

    }

    @Override
    public TransactionRepository
    createTransactionRepository() {

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
    public void close() throws SQLException {

        connection.close();
    }
}
