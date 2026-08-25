package com.rgoncalo.financialapp.bootstrap;

/**
 * Creates a transaction. Either {@code origin} or {@code target} may be
 * omitted for money entering or leaving the financial context.
 */
public record CreateTransactionBootstrapCommand(
        String context,
        String origin,
        String target,
        String dateTime,
        String value
) implements BootstrapCommand {
}
