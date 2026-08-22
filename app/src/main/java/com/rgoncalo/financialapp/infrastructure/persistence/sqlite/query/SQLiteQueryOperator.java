package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.query;

public enum SQLiteQueryOperator {

    EQUALS("="),
    NOT_EQUALS("!="),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUALS(">="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUALS("<=");

    private final String sql;

    SQLiteQueryOperator(
            String sql
    ) {
        this.sql = sql;
    }

    public String sql() {
        return sql;
    }
}