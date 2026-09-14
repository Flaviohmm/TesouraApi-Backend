package com.tesoura.api.professional.dto;

import com.tesoura.api.professional.Professional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProfessionalResponseDTO(
        UUID id,
        UUID userId,
        String name,
        String photoUrl,
        String bio,
        BigDecimal commissionRate,
        boolean isActive,
        Instant createdAt,
        List<ScheduleDTO> schedules
) {

    public static ProfessionalResponseDTO from(Professional professional, List<ScheduleDTO> schedules) {
        return new ProfessionalResponseDTO(
                professional.getId(),
                professional.getUserId(),
                professional.getName(),
                professional.getPhotoUrl(),
                professional.getBio(),
                professional.getCommissionRate(),
                professional.isActive(),
                professional.getCreatedAt(),
                schedules
        );
    }
}
