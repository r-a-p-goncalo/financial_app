package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.rgoncalo.financialapp.infrastructure.persistence.PersistenceException;

public class SQLiteSchema {

    static final String ACCOUNT_TABLE_NAME = "account";
    static final String FINANCIAL_CONTEXT_TABLE_NAME = "financialContext";

    static final String ACCOUNT_ID_COLUMN_NAME = "accountId"; //this has to match AccountRecordId
    static final String NAME_COLUMN_NAME = "name";
    static final String INITIAL_AMOUNT_VALUE_COLUMN_NAME = "initialAmount";
    static final String FINANCIAL_CONTEXT_ID_COLUMN_NAME = "financialContextId"; //this has to match FinancialContextId

    public static void initAccountRep(Connection connection) {

        String accountTable = """
                CREATE TABLE IF NOT EXISTS %s (
                    %s TEXT PRIMARY KEY,
                    %s TEXT NOT NULL,
                    %s DECIMAL NOT NULL,
                    %s TEXT NOT NULL,
                    FOREIGN KEY (%s)
                        REFERENCES %s(%s)
                )
                """.formatted(
                ACCOUNT_TABLE_NAME,
                ACCOUNT_ID_COLUMN_NAME,
                NAME_COLUMN_NAME,
                INITIAL_AMOUNT_VALUE_COLUMN_NAME,
                FINANCIAL_CONTEXT_ID_COLUMN_NAME,
                FINANCIAL_CONTEXT_ID_COLUMN_NAME,
                FINANCIAL_CONTEXT_TABLE_NAME,
                ACCOUNT_ID_COLUMN_NAME
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

    public static void initFinancialContextRep(Connection connection) {

        String financialContextTable = """
                CREATE TABLE IF NOT EXISTS %s (
                    %s TEXT PRIMARY KEY,
                    %s TEXT NOT NULL
                )
                """.formatted(
                FINANCIAL_CONTEXT_TABLE_NAME,
                ACCOUNT_ID_COLUMN_NAME,
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
    }
}
