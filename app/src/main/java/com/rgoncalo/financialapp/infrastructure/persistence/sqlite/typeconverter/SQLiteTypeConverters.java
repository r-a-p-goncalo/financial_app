package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.typeconverter;

import java.util.HashMap;
import java.util.Map;

public class SQLiteTypeConverters {

    private final Map<Class<?>, SQLiteTypeConverter<?>> converters;

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

        register(
                new MonetaryValueSQLiteTypeConverter()
        );

        register(
                new InstantSQLiteTypeConverter()
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

    public String sqliteType(
            Class<?> type
    ) {

        SQLiteTypeConverter<?> converterOfType = converters.get(type);

        if(converterOfType == null)
            throw new IllegalArgumentException("There is no defined type converter for type " + type.toString());

        return converterOfType.sqliteType();

    }


}