package com.tesoura.api.professional.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProfessionalRequestDTO(
        @NotBlank @Size(max = 120) String name,
        String photoUrl,
        String bio,
        BigDecimal commissionRate,
        Boolean isActive,
        UUID userId,
        @Valid List<ScheduleDTO> schedules
) {
}
