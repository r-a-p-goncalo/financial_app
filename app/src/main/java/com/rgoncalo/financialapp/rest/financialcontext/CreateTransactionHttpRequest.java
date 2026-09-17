package com.rgoncalo.financialapp.rest.financialcontext;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateTransactionHttpRequest(
        String originAccountId,
        String targetAccountId,
        @NotNull(message = "is required") Instant dateTime,
        @NotNull(message = "is required") BigDecimal value
) {
}
