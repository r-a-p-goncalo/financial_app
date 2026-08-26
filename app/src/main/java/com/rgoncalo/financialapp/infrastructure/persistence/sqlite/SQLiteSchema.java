package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import com.rgoncalo.financialapp.infrastructure.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
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
                    CREATE TABLE IF NOT EXISTS financial_contexts (
                        financial_context_id TEXT PRIMARY KEY,
                        name TEXT NOT NULL
                    )
                    """,
            """
                    CREATE TABLE IF NOT EXISTS accounts (
                        financial_context_id TEXT NOT NULL,
                        account_id TEXT NOT NULL,
                        name TEXT NOT NULL,
                        initial_amount TEXT NOT NULL,
                        PRIMARY KEY (financial_context_id, account_id)
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
                        PRIMARY KEY (financial_context_id, transaction_id),
                        CHECK (
                            origin_account_id IS NOT NULL
                            OR target_account_id IS NOT NULL
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
        } catch (SQLException exception) {
            throw new PersistenceException(
                    "Could not initialize SQLite schema",
                    exception
            );
        }
    }
}
