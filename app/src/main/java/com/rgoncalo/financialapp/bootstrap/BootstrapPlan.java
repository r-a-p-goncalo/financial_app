package com.rgoncalo.financialapp.bootstrap;

import java.util.List;

/**
 * Deserialized contents of a bootstrap configuration file.
 */
public record BootstrapPlan(
        BootstrapMode mode,
        List<BootstrapCommand> commands
) {

    public BootstrapPlan {
        mode = mode == null ? BootstrapMode.IF_EMPTY : mode;
        commands = commands == null ? List.of() : List.copyOf(commands);
    }
}
