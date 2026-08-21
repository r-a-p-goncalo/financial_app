package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support.typeconverter.SQLiteTypeConverters;

public class RecordConstructor {

    private final SQLiteTypeConverters typeConverters;

    public RecordConstructor(
            SQLiteTypeConverters typeConverters
    ) {
        this.typeConverters =
                typeConverters;
    }

    public <T> T construct(
            Class<T> type,
            ResultSet resultSet
    ) throws SQLException {

        return construct(
                type,
                "",
                resultSet
        );
    }

    private <T> T construct(
            Class<T> type,
            String prefix,
            ResultSet resultSet
    ) throws SQLException {

        if (!type.isRecord()) {

            throw new IllegalArgumentException(
                    "Expected record type but got: "
                            + type
            );
        }

        try {

            RecordComponent[] components =
                    type.getRecordComponents();

            Class<?>[] parameterTypes =
                    new Class<?>[
                            components.length
                            ];

            Object[] arguments =
                    new Object[
                            components.length
                            ];

            for (
                    int index = 0;
                    index < components.length;
                    index++
            ) {

                RecordComponent component =
                        components[index];

                Class<?> componentType =
                        component.getType();

                parameterTypes[index] =
                        componentType;

                String columnName =
                        prefix.isEmpty()
                                ? component.getName()
                                : prefix + "_"
                                  + component.getName();

                if (componentType.isRecord()) {

                    arguments[index] =
                            construct(
                                    componentType,
                                    columnName,
                                    resultSet
                            );

                } else {

                    Object databaseValue =
                            resultSet.getObject(
                                    columnName
                            );

                    arguments[index] =
                            typeConverters.fromDatabase(
                                    componentType,
                                    databaseValue
                            );
                }
            }

            Constructor<T> constructor =
                    type.getDeclaredConstructor(
                            parameterTypes
                    );

            return constructor.newInstance(
                    arguments
            );

        } catch (
                ReflectiveOperationException exception
        ) {

            throw new RuntimeException(
                    "Could not construct record: "
                            + type.getName(),
                    exception
            );
        }
    }
}