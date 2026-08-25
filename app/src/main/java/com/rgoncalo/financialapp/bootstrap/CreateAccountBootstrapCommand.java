package com.rgoncalo.financialapp.bootstrap;

/**
 * Creates an account in a bootstrap financial-context reference.
 */
public record CreateAccountBootstrapCommand(
        String context,
        String ref,
        String name,
        String initialAmount
) implements BootstrapCommand {
}
