package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support;

public record ColumnDefinition(
        String name,
        Class<?> type
) {}