package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support.typeconverter;

public class StringSQLiteTypeConverter
        implements SQLiteTypeConverter<String> {

    @Override
    public Class<String> type() {
        return String.class;
    }

    @Override
    public Object toDatabase(
            String value
    ) {
        return value;
    }

    @Override
    public String fromDatabase(
            Object value
    ) {
        return value.toString();
    }
}