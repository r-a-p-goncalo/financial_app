package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import com.rgoncalo.financialapp.infrastructure.persistence.PersistenceException;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.query.SQLiteQuery;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.query.SQLiteQueryBuilder;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.query.SQLiteQueryCondition;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.query.SQLiteQueryGenerator;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.record.RecordConstructor;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.record.RecordFlattener;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.typeconverter.SQLiteTypeConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SQLiteRepository<T> {

    private final Connection connection;

    private final Class<T> recordType;

    private final String tableName;

    private final SQLiteQueryGenerator
            queryGenerator;

    private final SQLiteTypeConverters
            typeConverters;

    private final RecordConstructor
            recordConstructor;

    private static final Logger logger =
            LoggerFactory.getLogger(
                    SQLiteRepository.class
            );

    public SQLiteRepository(
            Connection connection,
            Class<T> recordType,
            String tableName,
            SQLiteTypeConverters typeConverters
    ) {

        this.connection =
                connection;

        this.recordType =
                recordType;

        this.tableName =
                tableName;

        this.typeConverters =
                typeConverters;

        this.queryGenerator =
                new SQLiteQueryGenerator();

        this.recordConstructor =
                new RecordConstructor(
                        typeConverters
                );
    }

    public T save(
            T record
    ) {

        Map<String, Object> values =
                RecordFlattener.flatten(
                        record
                );

        String sql =
                queryGenerator.createInsertSql(
                        tableName,
                        values.keySet()
                );

        logger.info("Executing save query:\n{}", sql);

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            bindValues(
                    statement,
                    values.values()
            );

            statement.executeUpdate();

            return record;

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Could not save record in table: "
                            + tableName,
                    exception
            );
        }
    }

    public Optional<T> findSingleByRecordValue(Object id) {

        Collection<T> accountRecordsWithId = findByRecordValues(id);

        if (accountRecordsWithId.size() > 1) {
            throw new PersistenceException("Multiple records were gotten with ID");

        } else if (accountRecordsWithId.size() == 1) {
            return Optional.of(accountRecordsWithId.iterator().next());

        } else {
            return Optional.empty();
        }

    }

    public Collection<T> getAllRecords(){
        SQLiteQueryBuilder builder =
                new SQLiteQueryBuilder();
        builder.build();

        SQLiteQuery query =
                builder.build();

        return find(query);
    }

    public Collection<T> findByRecordValues(
            Object recordValue
    ) {

        Map<String, Object> idValues =
                RecordFlattener.flatten(
                        recordValue
                );

        SQLiteQueryBuilder builder =
                new SQLiteQueryBuilder();

        for (
                Map.Entry<String, Object> entry
                : idValues.entrySet()
        ) {

            builder.where(
                    entry.getKey(),
                    entry.getValue()
            );
        }

        SQLiteQuery query =
                builder.build();

       return find(query);
    }

    public Collection<T> find(
            SQLiteQuery query
    ) {

        String sql =
                queryGenerator.createSelectSql(
                        tableName,
                        query
                );

        logger.info("Executing find query:\n{}", sql);

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            bindQuery(
                    statement,
                    query
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                List<T> records =
                        new ArrayList<>();

                while (resultSet.next()) {

                    records.add(
                            recordConstructor.construct(
                                    recordType,
                                    resultSet
                            )
                    );
                }

                return records;
            }

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Could not execute query: "
                            + sql,
                    exception
            );
        }
    }

    public void delete(
            Object id
    ) {

        Map<String, Object> idValues =
                RecordFlattener.flatten(
                        id
                );

        SQLiteQueryBuilder builder =
                new SQLiteQueryBuilder();

        for (
                Map.Entry<String, Object> entry
                : idValues.entrySet()
        ) {

            builder.where(
                    entry.getKey(),
                    entry.getValue()
            );
        }

        SQLiteQuery query =
                builder.build();

        String sql =
                queryGenerator.createDeleteSql(
                        tableName,
                        query
                );

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            bindQuery(
                    statement,
                    query
            );

            statement.executeUpdate();

        } catch (SQLException exception) {

            throw new RuntimeException(
                    "Could not delete record",
                    exception
            );
        }
    }

    private void bindValues(
            PreparedStatement statement,
            Collection<Object> values
    ) throws SQLException {

        int index = 1;

        for (Object value : values) {

            statement.setObject(
                    index++,
                    typeConverters.toDatabase(
                            value
                    )
            );
        }
    }

    private void bindQuery(
            PreparedStatement statement,
            SQLiteQuery query
    ) throws SQLException {

        int index = 1;

        for (
                SQLiteQueryCondition condition
                : query.conditions()
        ) {

            statement.setObject(
                    index++,
                    typeConverters.toDatabase(
                            condition.value()
                    )
            );
        }
    }
}