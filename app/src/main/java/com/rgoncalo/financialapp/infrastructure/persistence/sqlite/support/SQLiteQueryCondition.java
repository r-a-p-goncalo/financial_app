package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support;

public record SQLiteQueryCondition(
        String column,
        SQLiteQueryOperator operator,
        Object value
) {}