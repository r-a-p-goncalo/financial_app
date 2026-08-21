package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support.typeconverter;

import java.time.Instant;

public class InstantSQLiteTypeConverter
        implements SQLiteTypeConverter<Instant> {

    @Override
    public Class<Instant> type() {
        return Instant.class;
    }

    @Override
    public Object toDatabase(
            Instant value
    ) {
        return value.toString();
    }

    @Override
    public Instant fromDatabase(
            Object value
    ) {
        return Instant.parse(
                value.toString()
        );
    }
}