package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support.typeconverter;

import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support.typeconverter.DoubleSQLiteTypeConverter;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support.typeconverter.IntegerSQLiteTypeConverter;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support.typeconverter.SQLiteTypeConverter;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support.typeconverter.StringSQLiteTypeConverter;

import java.util.HashMap;
import java.util.Map;

public class SQLiteTypeConverters {

    private final Map<
            Class<?>,
            SQLiteTypeConverter<?>
            > converters;

    public SQLiteTypeConverters() {

        this.converters =
                new HashMap<>();

        register(
                new StringSQLiteTypeConverter()
        );

        register(
                new IntegerSQLiteTypeConverter()
        );


        register(
                new DoubleSQLiteTypeConverter()
        );

    }

    public <T> void register(
            SQLiteTypeConverter<T> converter
    ) {

        converters.put(
                converter.type(),
                converter
        );
    }

    @SuppressWarnings("unchecked")
    public <T> SQLiteTypeConverter<T> get(
            Class<T> type
    ) {

        SQLiteTypeConverter<?> converter =
                converters.get(type);

        if (converter == null) {

            throw new IllegalArgumentException(
                    "No SQLite converter registered for: "
                            + type.getName()
            );
        }

        return (SQLiteTypeConverter<T>) converter;
    }

    public Object toDatabase(
            Object value
    ) {

        if (value == null) {
            return null;
        }

        SQLiteTypeConverter<Object> converter =
                (SQLiteTypeConverter<Object>) get(value.getClass());

        return converter.toDatabase(value);
    }

    public <T> T fromDatabase(
            Class<T> type,
            Object value
    ) {

        if (value == null) {
            return null;
        }

        SQLiteTypeConverter<T> converter =
                get(type);

        return converter.fromDatabase(value);
    }
}