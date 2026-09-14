package com.tesoura.api.service_catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record HairServiceRequestDTO(
        @NotBlank @Size(max = 120) String name,
        String description,
        @NotNull @Positive Integer durationMinutes,
        @NotNull @Positive BigDecimal price,
        @Size(max = 60) String category,
        Boolean isActive
) {
}
