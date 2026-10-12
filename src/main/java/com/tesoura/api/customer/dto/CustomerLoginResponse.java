package com.tesoura.api.customer.dto;

import java.util.UUID;

public record CustomerLoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UUID customerId,
        String name
) {
}
