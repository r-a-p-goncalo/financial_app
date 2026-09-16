package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import com.rgoncalo.financialapp.application.user.UserRepository;
import com.rgoncalo.financialapp.commondata.user.UserId;
import com.rgoncalo.financialapp.commondata.user.UserRecord;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

public class SQLiteUserRepository implements UserRepository {

    private static final String INSERT = """
            INSERT INTO users (user_id, name)
            VALUES (?, ?)
            ON CONFLICT(user_id) DO UPDATE SET
                name = excluded.name
            """;

    private static final String SELECT_ALL = """
            SELECT user_id, name
            FROM users
            """;

    private static final String SELECT_BY_ID = """
            SELECT user_id, name
            FROM users
            WHERE user_id = ?
            """;

    private final SQLiteRepository<UserRecord> sqliteRepository;

    public SQLiteUserRepository(Connection connection) {
        this.sqliteRepository = new SQLiteRepository<>(
                connection,
                SQLiteUserRepository::mapUser
        );
    }

    @Override
    public UserRecord save(UserRecord user) {
        return sqliteRepository.save(
                INSERT,
                user,
                user.userId().userId(),
                user.name()
        );
    }

    @Override
    public Collection<UserRecord> listUsersSummary() {
        return sqliteRepository.find(SELECT_ALL);
    }

    @Override
    public Optional<UserRecord> findById(UserId userId) {
        return sqliteRepository.findSingle(SELECT_BY_ID, userId.userId());
    }

    private static UserRecord mapUser(ResultSet resultSet)
            throws SQLException {
        return new UserRecord(
                new UserId(resultSet.getString("user_id")),
                resultSet.getString("name")
        );
    }
}
