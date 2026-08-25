package com.rgoncalo.financialapp.bootstrap;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * One ordered server-side operation defined in a bootstrap plan.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(
                value = CreateFinancialContextBootstrapCommand.class,
                name = "create-financial-context"
        ),
        @JsonSubTypes.Type(
                value = CreateAccountBootstrapCommand.class,
                name = "create-account"
        ),
        @JsonSubTypes.Type(
                value = CreateTransactionBootstrapCommand.class,
                name = "create-transaction"
        )
})
public sealed interface BootstrapCommand permits
        CreateFinancialContextBootstrapCommand,
        CreateAccountBootstrapCommand,
        CreateTransactionBootstrapCommand {
}
