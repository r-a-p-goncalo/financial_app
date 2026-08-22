package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.query;

public record SQLiteQueryCondition(
        String column,
        SQLiteQueryOperator operator,
        Object value
) {}