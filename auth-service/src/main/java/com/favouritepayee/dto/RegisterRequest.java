package com.favouritepayee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

public record RegisterRequest(
        @NotNull(message = "customerId is required")
        @Min(value = 1, message = "customerId must be greater than 0")
        Long customerId,
        
        @NotBlank(message = "name is required") 
        String name,
        
        @NotBlank(message = "password is required")
        @Size(min = 6, message = "password must be at least 6 characters")
        String password
) {
}
