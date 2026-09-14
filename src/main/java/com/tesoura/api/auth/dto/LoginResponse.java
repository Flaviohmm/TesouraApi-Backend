package com.tesoura.api.auth.dto;

import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UUID userId,
        UUID salonId,
        String name,
        String role
) {
}
