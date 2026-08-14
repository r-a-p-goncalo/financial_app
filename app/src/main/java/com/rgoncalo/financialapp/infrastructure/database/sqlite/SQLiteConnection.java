package com.rgoncalo.financialapp.infrastructure.database.sqlite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnection {

    private final static boolean DELETE_DATABASE_IF_EXISTS = true;

    private final Connection connection;

    public SQLiteConnection(String databasePath) {

        Path path = Path.of(databasePath);

        try {
            Path parent = path.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            if (DELETE_DATABASE_IF_EXISTS) {
                Files.deleteIfExists(path);
            }

            this.connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + path
            );

        } catch (IOException | SQLException exception) {
            throw new RuntimeException(
                    "Could not connect to SQLite database.",
                    exception
            );
        }
    }

    public Connection getConnection() {
        return connection;
    }
}