package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import com.rgoncalo.financialapp.application.user.UserRepository;
import com.rgoncalo.financialapp.commondata.user.UserId;
import com.rgoncalo.financialapp.commondata.user.PasswordHash;
import com.rgoncalo.financialapp.commondata.user.UserRecord;
import com.rgoncalo.financialapp.infrastructure.persistence.jdbc.JdbcRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

public class SQLiteUserRepository implements UserRepository {

    private static final String INSERT = """
            INSERT INTO users (
                user_id, name, password_hashing_strategy, password_hash
            ) VALUES (?, ?, ?, ?)
            ON CONFLICT(user_id) DO UPDATE SET
                name = excluded.name,
                password_hashing_strategy = excluded.password_hashing_strategy,
                password_hash = excluded.password_hash
            """;

    private static final String SELECT_ALL = """
            SELECT user_id, name, password_hashing_strategy, password_hash
            FROM users
            """;

    private static final String SELECT_BY_ID = """
            SELECT user_id, name, password_hashing_strategy, password_hash
            FROM users
            WHERE user_id = ?
            """;

    private static final String SELECT_BY_NAME = """
            SELECT user_id, name, password_hashing_strategy, password_hash
            FROM users
            WHERE name = ?
            """;

    private final JdbcRepository<UserRecord> jdbcRepository;

    public SQLiteUserRepository(DataSource dataSource) {
        this.jdbcRepository = new JdbcRepository<>(
                dataSource,
                SQLiteUserRepository::mapUser
        );
    }

    @Override
    public UserRecord save(UserRecord user) {
        return jdbcRepository.save(
                INSERT,
                user,
                user.userId().userId(),
                user.name(),
                passwordStrategyId(user),
                passwordHashValue(user)
        );
    }

    @Override
    public Collection<UserRecord> listUsersSummary() {
        return jdbcRepository.find(SELECT_ALL);
    }

    @Override
    public Optional<UserRecord> findById(UserId userId) {
        return jdbcRepository.findSingle(SELECT_BY_ID, userId.userId());
    }

    @Override
    public Optional<UserRecord> findByName(String name) {
        return jdbcRepository.findSingle(SELECT_BY_NAME, name);
    }

    private static UserRecord mapUser(ResultSet resultSet)
            throws SQLException {
        return new UserRecord(
                new UserId(resultSet.getString("user_id")),
                resultSet.getString("name"),
                passwordHash(resultSet)
        );
    }

    private static String passwordStrategyId(UserRecord user) {
        return user.passwordHash() == null
                ? null
                : user.passwordHash().strategyId();
    }

    private static String passwordHashValue(UserRecord user) {
        return user.passwordHash() == null
                ? null
                : user.passwordHash().value();
    }

    private static PasswordHash passwordHash(ResultSet resultSet)
            throws SQLException {
        String strategyId = resultSet.getString("password_hashing_strategy");
        String value = resultSet.getString("password_hash");

        if (strategyId == null && value == null) {
            return null;
        }

        if (strategyId == null || value == null) {
            throw new SQLException("User password hash is incomplete.");
        }

        return new PasswordHash(strategyId, value);
    }
}
