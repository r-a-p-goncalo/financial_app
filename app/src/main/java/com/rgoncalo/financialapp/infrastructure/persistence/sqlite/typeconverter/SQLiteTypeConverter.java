package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.typeconverter;

public interface SQLiteTypeConverter<T> {

    Class<T> type();

    String sqliteType();

    Object toDatabase(
            T value
    );

    T fromDatabase(
            Object value
    );
}