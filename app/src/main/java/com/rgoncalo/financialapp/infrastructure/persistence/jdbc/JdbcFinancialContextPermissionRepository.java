package com.rgoncalo.financialapp.infrastructure.persistence.jdbc;

import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.SQLiteFinancialContextPermissionRepository;

import javax.sql.DataSource;

/** Dialect-neutral JDBC financial-context-permission repository. */
public final class JdbcFinancialContextPermissionRepository
        extends SQLiteFinancialContextPermissionRepository {

    public JdbcFinancialContextPermissionRepository(DataSource dataSource) {
        super(dataSource);
    }
}
