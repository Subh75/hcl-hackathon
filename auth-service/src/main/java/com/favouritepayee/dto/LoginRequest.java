package com.favouritepayee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull(message = "customerId is required") Long customerId,
        @NotBlank(message = "password is required") String password
) {
}
