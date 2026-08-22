package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.typeconverter;

public class DoubleSQLiteTypeConverter
        implements SQLiteTypeConverter<Double> {

    @Override
    public Class<Double> type() {
        return Double.class;
    }

    @Override
    public Object toDatabase(
            Double value
    ) {
        return value;
    }

    @Override
    public String sqliteType() {
        return "REAL";
    }


    @Override
    public Double fromDatabase(
            Object value
    ) {

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        return Double.valueOf(
                value.toString()
        );
    }
}