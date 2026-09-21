package com.rgoncalo.financialapp.infrastructure.persistence.jdbc;

import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.SQLiteFinancialContextRepository;

import javax.sql.DataSource;

/** Dialect-neutral JDBC financial-context repository. */
public final class JdbcFinancialContextRepository
        extends SQLiteFinancialContextRepository {

    public JdbcFinancialContextRepository(DataSource dataSource) {
        super(dataSource);
    }
}
