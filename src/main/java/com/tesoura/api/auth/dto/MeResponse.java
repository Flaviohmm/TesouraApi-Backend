package com.tesoura.api.auth.dto;

import java.util.UUID;

public record MeResponse(
        UUID userId,
        UUID salonId,
        String name,
        String email,
        String phone,
        String role
) {
}
