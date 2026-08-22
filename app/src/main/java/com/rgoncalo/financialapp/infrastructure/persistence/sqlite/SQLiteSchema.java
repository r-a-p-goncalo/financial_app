package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import com.rgoncalo.financialapp.infrastructure.persistence.PersistenceException;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.schema.SQLiteCreateTableGenerator;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.schema.SQLiteTableDefinition;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.schema.SQLiteTableDefinitionFactory;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.typeconverter.SQLiteTypeConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.Collection;

public final class SQLiteSchema {

    private SQLiteSchema() {
    }

    private static final Logger logger =
            LoggerFactory.getLogger(
                    SQLiteSchema.class
            );

    public static void initialize(
            Connection connection,
            AbstractMap.SimpleEntry<Class<?>, Collection<String>>... recordTypesAndPrimaryKeys
    ) {

        SQLiteTypeConverters typeConverters =
                new SQLiteTypeConverters();

        SQLiteCreateTableGenerator generator =
                new SQLiteCreateTableGenerator(
                        typeConverters
                );

        for (
                AbstractMap.SimpleEntry<Class<?>, Collection<String>> recordTypeAndPrimaryKeys
                : recordTypesAndPrimaryKeys
        ) {

            Class<?> recordType = recordTypeAndPrimaryKeys.getKey();
            Collection<String> primaryKeys = recordTypeAndPrimaryKeys.getValue();

            createTable(
                    connection,
                    recordType,
                    primaryKeys,
                    generator
            );
        }
    }

    private static void createTable(
            Connection connection,
            Class<?> recordType,
            Collection<String> primaryKeys,
            SQLiteCreateTableGenerator generator
    ) {

        SQLiteTableDefinition table =
                SQLiteTableDefinitionFactory.create(
                        recordType
                );

        String sql =
                generator.generate(
                        table,
                        primaryKeys
                );

        logger.info("Executing table creation statement: {}", sql);

        try (
                Statement statement =
                        connection.createStatement()
        ) {

            statement.execute(
                    sql
            );

        } catch (SQLException exception) {

            throw new PersistenceException(
                    "Could not initialize table for record: "
                            + recordType.getName(),
                    exception
            );
        }
    }
}