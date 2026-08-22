package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.schema;


import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.record.ColumnDefinition;

import java.util.List;

public record SQLiteTableDefinition(
        String tableName,
        List<ColumnDefinition> columns
) {
}