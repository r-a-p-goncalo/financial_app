package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.record;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

public final class RecordStructure {

    private RecordStructure() {
    }

    public static List<ColumnDefinition> columns(
            Class<?> recordType
    ) {

        if (!recordType.isRecord()) {
            throw new IllegalArgumentException(
                    "Expected record type but got: "
                            + recordType
            );
        }

        List<ColumnDefinition> columns =
                new ArrayList<>();

        collectColumns(
                recordType,
                "",
                columns
        );

        return columns;
    }

    private static void collectColumns(
            Class<?> type,
            String prefix,
            List<ColumnDefinition> columns
    ) {

        for (RecordComponent component
                : type.getRecordComponents()) {

            String columnName =
                    prefix.isEmpty()
                            ? component.getName()
                            : prefix + "_"
                              + component.getName();

            Class<?> componentType =
                    component.getType();

            if (componentType.isRecord()) {

                collectColumns(
                        componentType,
                        columnName,
                        columns
                );

            } else {

                columns.add(
                        new ColumnDefinition(
                                columnName,
                                componentType
                        )
                );
            }
        }
    }
}