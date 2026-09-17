package com.rgoncalo.financialapp.rest.financialcontext;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateAccountHttpRequest(
        @NotBlank(message = "is required") String name,
        @NotNull(message = "is required") BigDecimal initialAmount
) {
}
