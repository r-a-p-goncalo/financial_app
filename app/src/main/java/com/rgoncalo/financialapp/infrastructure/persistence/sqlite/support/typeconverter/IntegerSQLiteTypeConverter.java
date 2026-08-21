package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support.typeconverter;


public class IntegerSQLiteTypeConverter
        implements SQLiteTypeConverter<Integer> {

    @Override
    public Class<Integer> type() {
        return Integer.class;
    }

    @Override
    public Object toDatabase(
            Integer value
    ) {
        return value;
    }

    @Override
    public Integer fromDatabase(
            Object value
    ) {

        if (value instanceof Number number) {
            return number.intValue();
        }

        return Integer.valueOf(
                value.toString()
        );
    }
}