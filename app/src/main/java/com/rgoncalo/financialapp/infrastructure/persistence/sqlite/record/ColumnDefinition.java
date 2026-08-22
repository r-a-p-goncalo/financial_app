package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.record;

public record ColumnDefinition(
        String name,
        Class<?> type
) {}