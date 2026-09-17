package com.rgoncalo.financialapp.rest.financialcontext;

import jakarta.validation.constraints.NotBlank;

public record CreateFinancialContextHttpRequest(
        @NotBlank(message = "is required") String name
) {
}
