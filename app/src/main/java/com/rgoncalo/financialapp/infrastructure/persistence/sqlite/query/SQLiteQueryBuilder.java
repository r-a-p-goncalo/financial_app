package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.query;

import java.util.ArrayList;
import java.util.List;

public class SQLiteQueryBuilder {

    private final List<SQLiteQueryCondition>
            conditions;

    public SQLiteQueryBuilder() {

        this.conditions =
                new ArrayList<>();
    }

    public SQLiteQueryBuilder where(
            String column,
            Object value
    ) {

        return where(
                column,
                SQLiteQueryOperator.EQUALS,
                value
        );
    }

    public SQLiteQueryBuilder where(
            String column,
            SQLiteQueryOperator operator,
            Object value
    ) {

        conditions.add(
                new SQLiteQueryCondition(
                        column,
                        operator,
                        value
                )
        );

        return this;
    }

    public SQLiteQuery build() {

        return new SQLiteQuery(
                conditions
        );
    }
}