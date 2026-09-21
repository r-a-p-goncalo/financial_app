package com.rgoncalo.financialapp.infrastructure.persistence.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Creates the correctly configured JDBC data source for a supported dialect. */
public final class DatabaseDataSourceFactory {

    private DatabaseDataSourceFactory() {
    }

    public static DataSource create(
            DatabaseDialect dialect,
            String jdbcUrl,
            String username,
            String password,
            int maximumPoolSize
    ) {
        dialect.validateJdbcUrl(jdbcUrl);

        return switch (dialect) {
            case POSTGRESQL -> postgreSqlDataSource(
                    jdbcUrl,
                    username,
                    password,
                    maximumPoolSize
            );
            case SQLITE -> sqliteDataSource(jdbcUrl);
        };
    }

    private static DataSource postgreSqlDataSource(
            String jdbcUrl,
            String username,
            String password,
            int maximumPoolSize
    ) {
        HikariConfig configuration = new HikariConfig();
        configuration.setJdbcUrl(jdbcUrl);
        configuration.setMaximumPoolSize(maximumPoolSize);
        configuration.setMinimumIdle(0);
        configuration.setPoolName("financial-app-postgresql");

        if (username != null && !username.isBlank()) {
            configuration.setUsername(username);
        }

        if (password != null && !password.isBlank()) {
            configuration.setPassword(password);
        }

        return new HikariDataSource(configuration);
    }

    private static DataSource sqliteDataSource(String jdbcUrl) {
        createSQLiteParentDirectory(jdbcUrl);

        SQLiteConfig configuration = new SQLiteConfig();
        configuration.enforceForeignKeys(true);

        SQLiteDataSource dataSource = new SQLiteDataSource(configuration);
        dataSource.setUrl(jdbcUrl);
        return dataSource;
    }

    private static void createSQLiteParentDirectory(String jdbcUrl) {
        String databasePath = jdbcUrl.substring("jdbc:sqlite:".length());

        if (databasePath.equals(":memory:") || databasePath.startsWith("file:")) {
            return;
        }

        try {
            Path parent = Path.of(databasePath).getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not create SQLite database directory.",
                    exception
            );
        }
    }
}
