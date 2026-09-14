package com.tesoura.api.auth;

import com.tesoura.api.shared.enums.UserRole;

import java.util.UUID;

public record AuthPrincipal(UUID userId, UUID salonId, UserRole role, String email) {
}
