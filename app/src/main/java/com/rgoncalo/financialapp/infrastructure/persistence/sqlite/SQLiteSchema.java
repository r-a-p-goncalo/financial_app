package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import com.rgoncalo.financialapp.infrastructure.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

/**
 * Defines the SQLite schema explicitly.
 *
 * <p>Schema names are part of the persistence contract and are intentionally
 * not inferred from Java record names. A migration tool can replace the
 * ordered statements here when the application begins preserving user data
 * across schema versions.</p>
 */
public final class SQLiteSchema {

    private SQLiteSchema() {
    }

    private static final Logger logger =
            LoggerFactory.getLogger(
                    SQLiteSchema.class
            );

    private static final List<String> CREATE_STATEMENTS = List.of(
            """
                    CREATE TABLE IF NOT EXISTS users (
                        user_id TEXT PRIMARY KEY,
                        name TEXT NOT NULL UNIQUE,
                        password_hashing_strategy TEXT,
                        password_hash TEXT,
                        CHECK (
                            (password_hashing_strategy IS NULL
                                AND password_hash IS NULL)
                            OR (password_hashing_strategy IS NOT NULL
                                AND password_hash IS NOT NULL)
                        )
                    )
                    """,
            """
                    CREATE TABLE IF NOT EXISTS financial_contexts (
                        financial_context_id TEXT PRIMARY KEY,
                        name TEXT NOT NULL,
                        parent_financial_context_id TEXT,
                        overridden_attributes INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (parent_financial_context_id)
                            REFERENCES financial_contexts(financial_context_id)
                    )
                    """,
            """
                    CREATE TABLE IF NOT EXISTS financial_context_permissions (
                        financial_context_id TEXT NOT NULL,
                        user_id TEXT NOT NULL,
                        permission TEXT NOT NULL CHECK (
                            permission IN ('READ', 'WRITE', 'OWNER')
                        ),
                        granted_by_user_id TEXT NOT NULL,
                        granted_at TEXT NOT NULL,
                        PRIMARY KEY (financial_context_id, user_id),
                        FOREIGN KEY (financial_context_id)
                            REFERENCES financial_contexts(financial_context_id),
                        FOREIGN KEY (user_id) REFERENCES users(user_id),
                        FOREIGN KEY (granted_by_user_id) REFERENCES users(user_id)
                    )
                    """,
            """
                    CREATE TABLE IF NOT EXISTS accounts (
                        financial_context_id TEXT NOT NULL,
                        account_id TEXT NOT NULL,
                        name TEXT NOT NULL,
                        initial_amount TEXT NOT NULL,
                        parent_financial_context_id TEXT,
                        parent_account_id TEXT,
                        overridden_attributes INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY (financial_context_id, account_id),
                        FOREIGN KEY (financial_context_id)
                            REFERENCES financial_contexts(financial_context_id),
                        FOREIGN KEY (
                            parent_financial_context_id, parent_account_id
                        ) REFERENCES accounts(financial_context_id, account_id)
                    )
                    """,
            """
                    CREATE TABLE IF NOT EXISTS transactions (
                        financial_context_id TEXT NOT NULL,
                        transaction_id TEXT NOT NULL,
                        origin_account_id TEXT,
                        target_account_id TEXT,
                        date_time TEXT NOT NULL,
                        value TEXT NOT NULL,
                        parent_financial_context_id TEXT,
                        parent_transaction_id TEXT,
                        overridden_attributes INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY (financial_context_id, transaction_id),
                        FOREIGN KEY (financial_context_id)
                            REFERENCES financial_contexts(financial_context_id),
                        FOREIGN KEY (
                            financial_context_id, origin_account_id
                        ) REFERENCES accounts(financial_context_id, account_id),
                        FOREIGN KEY (
                            financial_context_id, target_account_id
                        ) REFERENCES accounts(financial_context_id, account_id),
                        FOREIGN KEY (
                            parent_financial_context_id, parent_transaction_id
                        ) REFERENCES transactions(
                            financial_context_id, transaction_id
                        )
                    )
                    """,
            """
                    CREATE INDEX IF NOT EXISTS transactions_by_origin_account
                    ON transactions (financial_context_id, origin_account_id)
                    """,
            """
                    CREATE INDEX IF NOT EXISTS transactions_by_target_account
                    ON transactions (financial_context_id, target_account_id)
                    """
    );

    public static void initialize(Connection connection) {
        Objects.requireNonNull(connection);

        try (Statement statement = connection.createStatement()) {

            for (String sql : CREATE_STATEMENTS) {
                logger.info("Executing schema statement:\n{}", sql);
                statement.execute(sql);
            }

            createInheritanceIndexes(connection);
            ensureUserCredentialColumns(connection);
        } catch (SQLException exception) {
            throw new PersistenceException(
                    "Could not initialize SQLite schema",
                    exception
            );
        }
    }


    private static void createInheritanceIndexes(Connection connection)
            throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE INDEX IF NOT EXISTS financial_contexts_by_parent
                    ON financial_contexts (parent_financial_context_id)
                    """);
            statement.execute("""
                    CREATE INDEX IF NOT EXISTS financial_context_permissions_by_user
                    ON financial_context_permissions (user_id)
                    """);
            statement.execute("""
                    CREATE INDEX IF NOT EXISTS accounts_by_parent
                    ON accounts (
                        parent_financial_context_id, parent_account_id
                    )
                    """);
            statement.execute("""
                    CREATE INDEX IF NOT EXISTS transactions_by_parent
                    ON transactions (
                        parent_financial_context_id, parent_transaction_id
                    )
                    """);
        }
    }

    private static void ensureUserCredentialColumns(Connection connection)
            throws SQLException {
        if (!hasColumn(connection, "users", "password_hashing_strategy")) {
            executeSchemaChange(connection, """
                    ALTER TABLE users
                    ADD COLUMN password_hashing_strategy TEXT
                    """);
        }

        if (!hasColumn(connection, "users", "password_hash")) {
            executeSchemaChange(connection, """
                    ALTER TABLE users
                    ADD COLUMN password_hash TEXT
                    """);
        }
    }

    private static boolean hasColumn(
            Connection connection,
            String table,
            String column
    ) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "PRAGMA table_info(" + table + ")"
             )) {
            while (resultSet.next()) {
                if (column.equals(resultSet.getString("name"))) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void executeSchemaChange(
            Connection connection,
            String sql
    ) throws SQLException {
        logger.info("Executing schema statement:\n{}", sql);

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

}
