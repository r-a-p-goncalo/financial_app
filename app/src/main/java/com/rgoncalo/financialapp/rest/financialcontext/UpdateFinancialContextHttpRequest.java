package com.rgoncalo.financialapp.rest.financialcontext;

import jakarta.validation.constraints.NotBlank;

public record UpdateFinancialContextHttpRequest(
        @NotBlank(message = "is required") String name
) {
}
