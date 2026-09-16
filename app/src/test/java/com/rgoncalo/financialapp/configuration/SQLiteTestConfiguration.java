package com.rgoncalo.financialapp.configuration;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.application.user.UserRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextPermissionRepository;
import com.rgoncalo.financialapp.infrastructure.persistence.PersistenceException;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteTestConfiguration
        implements RepositoryTestConfiguration {

    private final Connection connection;

    public SQLiteTestConfiguration() {

        try {

            connection =
                    DriverManager.getConnection(
                            "jdbc:sqlite::memory:"
                    );

            SQLiteSchema.initialize(connection);

        } catch (SQLException exception) {

            throw new PersistenceException(
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

        return new SQLiteAccountRepository(connection);
    }

    @Override
    public FinancialContextRepository
    createFinancialContextRepository() {

        return new SQLiteFinancialContextRepository(connection);

    }

    @Override
    public TransactionRepository
    createTransactionRepository() {

        return new SQLiteTransactionRepository(connection);
    }

    @Override
    public UserRepository createUserRepository() {
        return new SQLiteUserRepository(connection);
    }

    @Override
    public FinancialContextPermissionRepository
    createFinancialContextPermissionRepository() {
        return new SQLiteFinancialContextPermissionRepository(connection);
    }

    @Override
    public void close() throws SQLException {

        connection.close();
    }
}
