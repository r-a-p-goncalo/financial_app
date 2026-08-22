package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.query;

import java.util.Collection;
import java.util.stream.Collectors;

public class SQLiteQueryGenerator {

    public String createInsertSql(
            String tableName,
            Collection<String> columns
    ) {

        String columnNames =
                String.join(
                        ", ",
                        columns
                );

        String placeholders =
                columns.stream()
                        .map(column -> "?")
                        .collect(
                                Collectors.joining(", ")
                        );

        return """
                INSERT INTO %s (%s)
                VALUES (%s)
                """.formatted(
                tableName,
                columnNames,
                placeholders
        );
    }

    public String createSelectSql(
            String tableName,
            SQLiteQuery query
    ) {

        StringBuilder sql =
                new StringBuilder();

        sql.append(
                "SELECT * FROM "
        );

        sql.append(tableName);

        if (!query.conditions().isEmpty()) {

            sql.append(" WHERE ");

            String conditions =
                    query.conditions()
                            .stream()
                            .map(
                                    condition ->
                                            condition.column()
                                                    + " "
                                                    + condition
                                                    .operator()
                                                    .sql()
                                                    + " ?"
                            )
                            .collect(
                                    Collectors.joining(
                                            " AND "
                                    )
                            );

            sql.append(conditions);
        }

        return sql.toString();
    }

    public String createDeleteSql(
            String tableName,
            SQLiteQuery query
    ) {

        if (query.conditions().isEmpty()) {

            throw new IllegalArgumentException(
                    "Delete query must have conditions"
            );
        }

        String conditions =
                query.conditions()
                        .stream()
                        .map(
                                condition ->
                                        condition.column()
                                                + " "
                                                + condition
                                                .operator()
                                                .sql()
                                                + " ?"
                        )
                        .collect(
                                Collectors.joining(
                                        " AND "
                                )
                        );

        return """
                DELETE FROM %s
                WHERE %s
                """.formatted(
                tableName,
                conditions
        );
    }
}