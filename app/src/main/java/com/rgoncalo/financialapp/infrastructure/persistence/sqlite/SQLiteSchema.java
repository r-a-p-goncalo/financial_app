package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.rgoncalo.financialapp.infrastructure.persistence.PersistenceException;

public class SQLiteSchema {

    static final String ACCOUNT_TABLE_NAME = "account";
    static final String FINANCIAL_CONTEXT_TABLE_NAME = "financialContext";
    static final String TRANSACTION_TABLE_NAME = "financialTransaction";

    static final String ACCOUNT_ID_COLUMN_NAME = "accountId"; //this has to match AccountRecordId
    static final String NAME_COLUMN_NAME = "name";
    static final String INITIAL_AMOUNT_VALUE_COLUMN_NAME = "initialAmount";

    static final String FINANCIAL_CONTEXT_ID_IN_REFERENCES = "financialContextId_financialContextId";

    static final String FINANCIAL_CONTEXT_ID_COLUMN_NAME = "financialContextId"; //this has to match FinancialContextId

    static final String TRANSACTION_ID = "transactionId";
    static final String TRANSACTION_VALUE = "transactionValue";
    static final String TRANSACTION_DATE_TIME_COLUMN_NAME = "dateTime";
    static final String TRANSACTION_ORIGIN_ACCOUNT_ID = "originAccountId_accountId";
    static final String TRANSACTION_TARGET_ACCOUNT_ID = "targetAccountId_accountId";

    public static void initAccountRep(Connection connection) {

        String accountTable = """
            CREATE TABLE IF NOT EXISTS %s (
                %s TEXT NOT NULL,
                %s TEXT NOT NULL,
                %s DECIMAL NOT NULL,
                %s TEXT NOT NULL,

                PRIMARY KEY (%s, %s),

                FOREIGN KEY (%s)
                    REFERENCES %s(%s)
            )
            """.formatted(
                ACCOUNT_TABLE_NAME,

                ACCOUNT_ID_COLUMN_NAME,
                NAME_COLUMN_NAME,
                INITIAL_AMOUNT_VALUE_COLUMN_NAME,
                FINANCIAL_CONTEXT_ID_IN_REFERENCES,

                ACCOUNT_ID_COLUMN_NAME,
                FINANCIAL_CONTEXT_ID_COLUMN_NAME,

                FINANCIAL_CONTEXT_ID_IN_REFERENCES,
                FINANCIAL_CONTEXT_TABLE_NAME,
                FINANCIAL_CONTEXT_ID_COLUMN_NAME
        );

        try (Statement statement = connection.createStatement()) {

            statement.execute(accountTable);

        } catch (SQLException exception) {

            throw new PersistenceException(
                    "Could not initialize table for account.",
                    exception
            );
        }
    }


    public static void initTransactionRep(Connection connection) {

        String transactionTable = """
            CREATE TABLE IF NOT EXISTS %s (
                %s TEXT NOT NULL,
                %s TEXT NOT NULL,
                %s TEXT NOT NULL,
                %s TEXT NOT NULL,
                %s TEXT NOT NULL,
                %s DECIMAL NOT NULL,

                PRIMARY KEY (%s, %s),

                FOREIGN KEY (%s)
                    REFERENCES %s(%s),

                FOREIGN KEY (%s)
                    REFERENCES %s(%s),

                FOREIGN KEY (%s)
                    REFERENCES %s(%s)
            )
            """.formatted(
                TRANSACTION_TABLE_NAME,

                TRANSACTION_ID,
                FINANCIAL_CONTEXT_ID_IN_REFERENCES,
                TRANSACTION_ORIGIN_ACCOUNT_ID,
                TRANSACTION_TARGET_ACCOUNT_ID,
                TRANSACTION_DATE_TIME_COLUMN_NAME,
                TRANSACTION_VALUE,

                TRANSACTION_ID,
                FINANCIAL_CONTEXT_ID_COLUMN_NAME,

                FINANCIAL_CONTEXT_ID_IN_REFERENCES,
                FINANCIAL_CONTEXT_TABLE_NAME,
                FINANCIAL_CONTEXT_ID_COLUMN_NAME,

                TRANSACTION_ORIGIN_ACCOUNT_ID,
                ACCOUNT_TABLE_NAME,
                ACCOUNT_ID_COLUMN_NAME,

                TRANSACTION_TARGET_ACCOUNT_ID,
                ACCOUNT_TABLE_NAME,
                ACCOUNT_ID_COLUMN_NAME
        );

        try (Statement statement = connection.createStatement()) {

            statement.execute(transactionTable);

        } catch (SQLException exception) {

            throw new PersistenceException(
                    "Could not initialize table for transaction.",
                    exception
            );
        }
    }

    public static void initFinancialContextRep(Connection connection) {

        String financialContextTable = """
                CREATE TABLE IF NOT EXISTS %s (
                    %s TEXT PRIMARY KEY,
                    %s TEXT NOT NULL
                )
                """.formatted(
                FINANCIAL_CONTEXT_TABLE_NAME,
                FINANCIAL_CONTEXT_ID_COLUMN_NAME,
                NAME_COLUMN_NAME
        );

        try (Statement statement = connection.createStatement()) {
            statement.execute(financialContextTable);
        } catch (SQLException exception) {
            throw new PersistenceException(
                    "Could not initialize table for financial context.",
                    exception
            );
        }
    }

    public static void initialize(Connection connection) {
        initFinancialContextRep(connection);
        initAccountRep(connection);
        initTransactionRep(connection);
    }
}
