package com.rgoncalo.financialapp.configuration;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.infrastructure.database.sqlite.SQLiteAccountRepository;
import com.rgoncalo.financialapp.infrastructure.database.sqlite.SQLiteFinancialContextRepository;
import com.rgoncalo.financialapp.infrastructure.database.sqlite.SQLiteSchema;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteTestConfiguration implements RepositoryTestConfiguration{

        private Connection connection;

        @Override
        public String name() {
            return "SQLite";
        }

    @Override
    public AccountRepository createAccountRepository() {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:sqlite::memory:"
            );

            initializeDatabase(connection);

            return new SQLiteAccountRepository(connection);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FinancialContextRepository createFinancialContextRepository() {

        try {
            connection = DriverManager.getConnection(
                    "jdbc:sqlite::memory:"
            );

            initializeDatabase(connection);

            return new SQLiteFinancialContextRepository(connection);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private void initializeDatabase(Connection connection)
                throws SQLException {

        SQLiteSchema.initialize(connection);}

    @Override
    public void close() throws SQLException {
            if (connection != null) {
                connection.close();
            }
        }
    }