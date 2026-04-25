package com.favouritepayee.dto;

public record LoginResponse(String token, String refreshToken, String role) {
}
