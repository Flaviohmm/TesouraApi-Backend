package com.tesoura.api.salon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SalonRequestDTO(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 20) String phone,
        String address,
        @Size(max = 50) String timezone,
        String logoUrl
) {
}
