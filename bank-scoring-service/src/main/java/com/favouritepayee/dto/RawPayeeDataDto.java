package com.favouritepayee.dto;

import java.util.List;

public record RawPayeeDataDto(
        Long payeeId,
        Long customerId,
        String name,
        String iban,
        String bank,
        List<RawInteractionDto> interactions
) {
}
