package com.favouritepayee.dto;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(@NotNull(message = "customerId is required") Long customerId) {
}
