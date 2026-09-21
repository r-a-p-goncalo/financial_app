package com.rgoncalo.financialapp.infrastructure.persistence.jdbc;

import java.util.Arrays;

/**
 * Database flavours explicitly supported by the application.
 *
 * <p>This is intentionally an enum rather than an arbitrary configuration
 * string. A dialect selects the compatible JDBC URL, migrations, and any SQL
 * differences that future repositories need to handle.</p>
 */
public enum DatabaseDialect {

    POSTGRESQL(
            "postgresql",
            "jdbc:postgresql:",
            "classpath:db/migration/postgresql"
    ),
    SQLITE(
            "sqlite",
            "jdbc:sqlite:",
            "classpath:db/migration/sqlite"
    );

    private final String configurationValue;
    private final String jdbcUrlPrefix;
    private final String migrationLocation;

    DatabaseDialect(
            String configurationValue,
            String jdbcUrlPrefix,
            String migrationLocation
    ) {
        this.configurationValue = configurationValue;
        this.jdbcUrlPrefix = jdbcUrlPrefix;
        this.migrationLocation = migrationLocation;
    }

    public static DatabaseDialect fromConfiguration(String value) {
        return Arrays.stream(values())
                .filter(dialect -> dialect.configurationValue.equalsIgnoreCase(
                        value
                ))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported financial-app.database.dialect: " + value
                ));
    }

    public void validateJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null || !jdbcUrl.startsWith(jdbcUrlPrefix)) {
            throw new IllegalArgumentException(
                    "The " + configurationValue + " dialect requires a JDBC URL "
                            + "starting with " + jdbcUrlPrefix
            );
        }
    }

    public String migrationLocation() {
        return migrationLocation;
    }
}
