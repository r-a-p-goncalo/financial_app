package com.rgoncalo.financialapp.infrastructure.persistence.jdbc;

import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.SQLiteTransactionRepository;

import javax.sql.DataSource;

/** Dialect-neutral JDBC transaction repository. */
public final class JdbcTransactionRepository
        extends SQLiteTransactionRepository {

    public JdbcTransactionRepository(DataSource dataSource) {
        super(dataSource);
    }
}
