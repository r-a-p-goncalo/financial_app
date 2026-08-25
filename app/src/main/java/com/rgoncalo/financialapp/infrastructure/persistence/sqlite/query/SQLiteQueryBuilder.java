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

        return addCondition(
                column,
                operator,
                value,
                SQLiteQueryJunction.AND
        );
    }

    public SQLiteQueryBuilder orWhere(
            String column,
            Object value
    ) {

        return orWhere(
                column,
                SQLiteQueryOperator.EQUALS,
                value
        );
    }

    public SQLiteQueryBuilder orWhere(
            String column,
            SQLiteQueryOperator operator,
            Object value
    ) {

        return addCondition(
                column,
                operator,
                value,
                SQLiteQueryJunction.OR
        );
    }

    private SQLiteQueryBuilder addCondition(
            String column,
            SQLiteQueryOperator operator,
            Object value,
            SQLiteQueryJunction junction
    ) {

        conditions.add(
                new SQLiteQueryCondition(
                        column,
                        operator,
                        value,
                        junction
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
