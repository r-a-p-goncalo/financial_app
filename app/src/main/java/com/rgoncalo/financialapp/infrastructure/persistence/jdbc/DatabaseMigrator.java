package com.rgoncalo.financialapp.infrastructure.persistence.jdbc;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;

import javax.sql.DataSource;

/** Applies the versioned schema for the configured database dialect. */
public final class DatabaseMigrator {

    private DatabaseMigrator() {
    }

    public static void migrate(DataSource dataSource, DatabaseDialect dialect) {
        FluentConfiguration configuration = Flyway.configure()
                .dataSource(dataSource)
                .locations(dialect.migrationLocation());

        if (dialect == DatabaseDialect.SQLITE) {
            // Existing SQLite prototype databases already match V1. The
            // baseline preserves their data while all new databases run V1.
            configuration.baselineOnMigrate(true).baselineVersion("1");
        }

        configuration.load().migrate();
    }
}
