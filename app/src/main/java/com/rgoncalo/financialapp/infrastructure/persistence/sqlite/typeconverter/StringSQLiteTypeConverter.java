package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.typeconverter;

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
    public String sqliteType() {
        return "TEXT";
    }

    @Override
    public String fromDatabase(
            Object value
    ) {
        return value.toString();
    }
}