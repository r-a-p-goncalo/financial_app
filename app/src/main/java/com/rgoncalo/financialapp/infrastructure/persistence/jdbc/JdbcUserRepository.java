package com.rgoncalo.financialapp.infrastructure.persistence.jdbc;

import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.SQLiteUserRepository;

import javax.sql.DataSource;

/** Dialect-neutral JDBC user repository. */
public final class JdbcUserRepository extends SQLiteUserRepository {

    public JdbcUserRepository(DataSource dataSource) {
        super(dataSource);
    }
}
