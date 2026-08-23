package com.rgoncalo.financialapp.infrastructure.persistence.sqlite;

import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.typeconverter.SQLiteTypeConverters;

import java.sql.Connection;

public class SQLiteRepositoryFactory<T> {

    public SQLiteRepository<T> sqLiteRepositoryOfType(Connection connection, Class<T> recordType){
        return new SQLiteRepository<T>(
                connection,
                recordType,
                SQLiteSchema.getInitializedTable(recordType).tableName(),
                new SQLiteTypeConverters()
        );
    }
}
