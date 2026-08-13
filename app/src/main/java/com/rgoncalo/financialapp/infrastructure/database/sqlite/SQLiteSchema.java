package com.rgoncalo.financialapp.infrastructure.database.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


public class SQLiteSchema {

    public static void initialize(Connection connection){

        String accountTable = """
                CREATE TABLE IF NOT EXISTS account (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    initial_amount_value DECIMAL NOT NULL
                )
                """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(accountTable);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Could not initialize database schema.",
                    exception
            );
        }
    }
}