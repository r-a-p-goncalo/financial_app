package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.typeconverter;

import com.rgoncalo.financialapp.commondata.money.MonetaryValue;

public class MonetaryValueSQLiteTypeConverter implements  SQLiteTypeConverter<MonetaryValue> {

    @Override
    public Class<MonetaryValue> type() {
        return MonetaryValue.class;
    }

    @Override
    public Object toDatabase(
            MonetaryValue value
    ) {
        return value.toString();
    }

    @Override
    public String sqliteType() {
        return "TEXT";
    }

    @Override
    public MonetaryValue fromDatabase(
            Object value
    ) {
        return MonetaryValue.parse(
                value.toString()
        );
    }
}
