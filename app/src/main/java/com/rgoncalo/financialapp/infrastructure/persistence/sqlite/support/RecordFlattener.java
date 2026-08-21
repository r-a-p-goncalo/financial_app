package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support;

import java.lang.reflect.RecordComponent;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RecordFlattener {

    private RecordFlattener() {
    }

    public static Map<String, Object> flatten(
            Object object
    ) {

        if (object == null) {
            throw new IllegalArgumentException(
                    "Cannot flatten null"
            );
        }

        Map<String, Object> values =
                new LinkedHashMap<>();

        flatten(
                object,
                "",
                values
        );

        return values;
    }

    private static void flatten(
            Object object,
            String prefix,
            Map<String, Object> currentRecordAttributeValues
    ) {

        Class<?> type =
                object.getClass();

        if (!type.isRecord()) {
            throw new IllegalArgumentException(
                    "Expected record but got: "
                            + type
            );
        }

        for (RecordComponent component
                : type.getRecordComponents()) {

            try {

                Object valueOfRecordAttribute =
                        component.getAccessor()
                                .invoke(object);

                String nameOfRecordAttribute =
                        prefix.isEmpty()
                                ? component.getName()
                                : prefix + "_"
                                  + component.getName();

                if (valueOfRecordAttribute != null
                        && valueOfRecordAttribute.getClass().isRecord()) {

                    flatten(
                            valueOfRecordAttribute,
                            nameOfRecordAttribute,
                            currentRecordAttributeValues
                    );

                } else {

                    currentRecordAttributeValues.put(
                            nameOfRecordAttribute,
                            valueOfRecordAttribute
                    );
                }

            } catch (ReflectiveOperationException exception) {

                throw new RuntimeException(
                        "Could not read record component: "
                                + component.getName(),
                        exception
                );
            }
        }
    }
}