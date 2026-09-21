package com.rgoncalo.financialapp.infrastructure.persistence.jdbc;

import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.SQLiteAccountRepository;

import javax.sql.DataSource;

/** Dialect-neutral JDBC account repository. */
public final class JdbcAccountRepository extends SQLiteAccountRepository {

    public JdbcAccountRepository(DataSource dataSource) {
        super(dataSource);
    }
}
