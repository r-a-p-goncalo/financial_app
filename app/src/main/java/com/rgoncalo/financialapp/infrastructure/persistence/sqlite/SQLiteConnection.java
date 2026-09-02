package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import com.rgoncalo.financialapp.infrastructure.persistence.PersistenceException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteConnection {

    private final Connection connection;

    public SQLiteConnection(String databasePath) {

        Path path = Path.of(databasePath);

        try {
            Path parent = path.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            this.connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + path
            );
            try (Statement statement = this.connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }

        } catch (IOException | SQLException exception) {
            throw new PersistenceException(
                    "Could not connect to SQLite database.",
                    exception
            );
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
