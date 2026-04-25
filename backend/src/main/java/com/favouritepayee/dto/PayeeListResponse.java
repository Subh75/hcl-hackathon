package com.favouritepayee.dto;

import java.util.List;

public record PayeeListResponse(
        List<PayeeDto> smartFavourites,
        PageResponse<PayeeDto> all
) {
}
