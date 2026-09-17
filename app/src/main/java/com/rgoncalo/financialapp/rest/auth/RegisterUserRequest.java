package com.rgoncalo.financialapp.rest.auth;

import jakarta.validation.constraints.NotBlank;

public record RegisterUserRequest(
        @NotBlank(message = "is required") String name,
        @NotBlank(message = "is required") String password
) {
}
