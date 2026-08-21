package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support;

import java.util.List;

public record SQLiteQuery(
        List<SQLiteQueryCondition> conditions
) {

    public SQLiteQuery {

        conditions =
                List.copyOf(conditions);
    }

    public static SQLiteQuery all() {

        return new SQLiteQuery(
                List.of()
        );
    }
}