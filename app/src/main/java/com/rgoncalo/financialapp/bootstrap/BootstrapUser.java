package com.rgoncalo.financialapp.bootstrap;

/**
 * Known local user created before a bootstrap plan adds its financial data.
 */
public record BootstrapUser(String name, String password) {

    public BootstrapUser {
        name = requireValue(name, "user name");
        password = requireValue(password, "user password");
    }

    private static String requireValue(String value, String description) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Bootstrap user requires " + description
            );
        }

        return value;
    }
}
