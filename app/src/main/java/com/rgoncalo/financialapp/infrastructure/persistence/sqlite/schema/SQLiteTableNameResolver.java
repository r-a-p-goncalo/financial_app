package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.schema;

public final class SQLiteTableNameResolver {

    private SQLiteTableNameResolver() {
    }

    public static String resolve(
            Class<?> recordType
    ) {

        return recordType.getSimpleName();

    }

}