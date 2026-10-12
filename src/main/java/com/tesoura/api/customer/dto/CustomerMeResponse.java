package com.tesoura.api.customer.dto;

import java.util.UUID;

public record CustomerMeResponse(
        UUID id,
        String name,
        String email,
        String phone
) {
}
