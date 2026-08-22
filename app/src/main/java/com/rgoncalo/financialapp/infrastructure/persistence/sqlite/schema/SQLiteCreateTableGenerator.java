package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.schema;

import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.record.ColumnDefinition;
import com.rgoncalo.financialapp.infrastructure.persistence.sqlite.typeconverter.SQLiteTypeConverters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SQLiteCreateTableGenerator {

    private final SQLiteTypeConverters
            typeConverters;

    public SQLiteCreateTableGenerator(
            SQLiteTypeConverters typeConverters
    ) {
        this.typeConverters =
                typeConverters;
    }

    public String generate(
            SQLiteTableDefinition table,
            Collection<String> primaryKeys
    ) {

        List<String> definitions =
                new ArrayList<>();

        for (
                ColumnDefinition column
                : table.columns()
        ) {

            String sqliteType =
                    typeConverters.sqliteType(
                            column.type()
                    );

            definitions.add(
                    """
                    %s %s NOT NULL
                    """.formatted(
                            column.name(),
                            sqliteType
                    ).trim()
            );
        }

        definitions.add(
                """
                PRIMARY KEY (%s)
                """.formatted(
                        String.join(
                                ", ",
                                primaryKeys
                        )
                ).trim()
        );

        return """
                CREATE TABLE IF NOT EXISTS %s (
                    %s
                )
                """.formatted(
                table.tableName(),
                String.join(
                        ",\n    ",
                        definitions
                )
        );
    }
}