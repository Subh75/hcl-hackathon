package com.favouritepayee.dto;

public record PayeeDto(
        Long id,
        String name,
        String iban,
        String bank,
        Double score
) {
}
