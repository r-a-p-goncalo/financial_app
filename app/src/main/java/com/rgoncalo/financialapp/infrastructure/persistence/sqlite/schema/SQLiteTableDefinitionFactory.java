package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.schema;


import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.record.ColumnDefinition;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.record.RecordStructure;

import java.util.List;

public final class SQLiteTableDefinitionFactory {

    private SQLiteTableDefinitionFactory() {
    }

    public static SQLiteTableDefinition create(
            Class<?> recordType
    ) {

        List<ColumnDefinition> columns =
                RecordStructure.columns(
                        recordType
                );


        String tableName =
                SQLiteTableNameResolver.resolve(
                        recordType
                );

        return new SQLiteTableDefinition(
                tableName,
                columns
        );
    }
}