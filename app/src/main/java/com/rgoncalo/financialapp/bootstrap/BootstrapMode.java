package com.rgoncalo.financialapp.bootstrap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

/**
 * Controls when configured bootstrap commands are executed.
 */
public enum BootstrapMode {

    NEVER,
    IF_EMPTY,
    ALWAYS;

    @JsonCreator
    public static BootstrapMode fromJson(String value) {

        return BootstrapMode.valueOf(
                value.toUpperCase(Locale.ROOT)
                        .replace('-', '_')
        );
    }

    @JsonValue
    public String toJson() {
        return name().toLowerCase(Locale.ROOT)
                .replace('_', '-');
    }
}
