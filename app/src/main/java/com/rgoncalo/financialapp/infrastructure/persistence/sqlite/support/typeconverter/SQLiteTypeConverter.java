package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support.typeconverter;

public interface SQLiteTypeConverter<T> {

    Class<T> type();

    Object toDatabase(
            T value
    );

    T fromDatabase(
            Object value
    );
}