package com.rgoncalo.financialapp.bootstrap;

/**
 * Creates a financial context and stores its generated ID under {@code ref}.
 */
public record CreateFinancialContextBootstrapCommand(
        String ref,
        String name
) implements BootstrapCommand {
}
