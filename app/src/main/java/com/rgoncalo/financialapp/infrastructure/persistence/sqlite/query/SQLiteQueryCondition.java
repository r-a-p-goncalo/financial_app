package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.query;

public record SQLiteQueryCondition(
        String column,
        SQLiteQueryOperator operator,
        Object value,
        SQLiteQueryJunction junction
) {

    public SQLiteQueryCondition(
            String column,
            SQLiteQueryOperator operator,
            Object value
    ) {
        this(column, operator, value, SQLiteQueryJunction.AND);
    }
}
