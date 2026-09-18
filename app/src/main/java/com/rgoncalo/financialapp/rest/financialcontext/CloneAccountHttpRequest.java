package com.rgoncalo.financialapp.rest.financialcontext;

import jakarta.validation.constraints.NotBlank;

public record CloneAccountHttpRequest(
        @NotBlank(message = "is required") String sourceAccountId,
        @NotBlank(message = "is required") String sourceFinancialContextId
) {
}
