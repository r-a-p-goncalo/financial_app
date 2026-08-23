package com.rgoncalo.financialapp.configuration;

import com.rgoncalo.financialapp.application.account.AccountRepository;
import com.rgoncalo.financialapp.application.financialcontext.FinancialContextRepository;
import com.rgoncalo.financialapp.application.transaction.TransactionRepository;
import com.rgoncalo.financialapp.commondata.account.AccountRecord;
import com.rgoncalo.financialapp.commondata.financialcontext.FinancialContextRecord;
import com.rgoncalo.financialapp.commondata.transaction.TransactionRecord;
import com.rgoncalo.financialapp.infrastructure.persistence.PersistenceException;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.List;

import static com.rgoncalo.financialapp.Main.*;

public class SQLiteTestConfiguration
        implements RepositoryTestConfiguration {

    private final Connection connection;

    public SQLiteTestConfiguration() {

        try {

            connection =
                    DriverManager.getConnection(
                            "jdbc:sqlite::memory:"
                    );

            SQLiteSchema.initialize(
                    connection,
                    new AbstractMap.SimpleEntry<>(FinancialContextRecord.class, List.of(FINANCIAL_CONTEXT_ID_STRING)),
                    new AbstractMap.SimpleEntry<>(AccountRecord.class, List.of(ACCOUNT_FINANCIAL_CONTEXT_ID_STRING, ACCOUNT_ID_STRING)),
                    new AbstractMap.SimpleEntry<>(TransactionRecord.class, List.of(TRANSACTION_FINANCIAL_CONTEXT_ID_STRING, TRANSACTION_ID_STRING))
            );

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

        return  new SQLiteAccountRepository(
                new SQLiteRepositoryFactory<AccountRecord>().sqLiteRepositoryOfType(connection, AccountRecord.class)
        );
    }

    @Override
    public FinancialContextRepository
    createFinancialContextRepository() {

        return new SQLiteFinancialContextRepository(
                new SQLiteRepositoryFactory<FinancialContextRecord>().sqLiteRepositoryOfType(connection, FinancialContextRecord.class)
        );

    }

    @Override
    public TransactionRepository
    createTransactionRepository() {

        return new SQLiteTransactionRepository(
                new SQLiteRepositoryFactory<TransactionRecord>().sqLiteRepositoryOfType(connection, TransactionRecord.class)
        );
    }

    @Override
    public void close() throws SQLException {

        connection.close();
    }
}