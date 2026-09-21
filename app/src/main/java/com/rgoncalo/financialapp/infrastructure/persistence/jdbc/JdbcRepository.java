package com.rgoncalo.financialapp.infrastructure.persistence.jdbc;

import com.rgoncalo.financialapp.infrastructure.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Small JDBC helper shared by dialect-neutral repository implementations.
 *
 * <p>Every operation borrows a connection from the data source, which makes
 * the same repositories safe for pooled PostgreSQL connections and SQLite.</p>
 */
public final class JdbcRepository<T> {

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet resultSet) throws SQLException;
    }

    private static final Logger logger = LoggerFactory.getLogger(
            JdbcRepository.class
    );

    private final DataSource dataSource;
    private final RowMapper<T> rowMapper;

    public JdbcRepository(DataSource dataSource, RowMapper<T> rowMapper) {
        this.dataSource = Objects.requireNonNull(dataSource);
        this.rowMapper = Objects.requireNonNull(rowMapper);
    }

    public T save(String sql, T record, Object... parameters) {
        executeUpdate(sql, parameters);
        return record;
    }

    public void executeUpdate(String sql, Object... parameters) {
        logger.info("Executing update query:\n{}", sql);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, parameters);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new PersistenceException(
                    "Could not execute update query: " + sql,
                    exception
            );
        }
    }

    public Collection<T> find(String sql, Object... parameters) {
        logger.info("Executing find query:\n{}", sql);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, parameters);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<T> records = new ArrayList<>();

                while (resultSet.next()) {
                    records.add(rowMapper.map(resultSet));
                }

                return List.copyOf(records);
            }
        } catch (SQLException exception) {
            throw new PersistenceException(
                    "Could not execute find query: " + sql,
                    exception
            );
        }
    }

    public Optional<T> findSingle(String sql, Object... parameters) {
        Collection<T> records = find(sql, parameters);

        if (records.size() > 1) {
            throw new PersistenceException(
                    "Expected at most one record but found " + records.size()
            );
        }

        return records.stream().findFirst();
    }

    private static void bind(PreparedStatement statement, Object... parameters)
            throws SQLException {
        for (int index = 0; index < parameters.length; index++) {
            statement.setObject(index + 1, parameters[index]);
        }
    }
}
