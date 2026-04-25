package com.favouritepayee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PayeeRequest(
        @NotBlank(message = "name is required")
        @Pattern(regexp = "^[a-zA-Z0-9 '\\-]+$", message = "name must match [a-zA-Z0-9 '\\-]+")
        String name,
        @NotBlank(message = "iban is required")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "iban must be alphanumeric")
        @Size(max = 20, message = "iban must be at most 20 characters")
        String iban
) {
}
