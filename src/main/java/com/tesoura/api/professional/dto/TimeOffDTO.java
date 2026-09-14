package com.tesoura.api.professional.dto;

import com.tesoura.api.professional.ProfessionalTimeOff;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record TimeOffDTO(
        UUID id,
        @NotNull Instant startsAt,
        @NotNull Instant endsAt,
        String reason
) {

    public static TimeOffDTO from(ProfessionalTimeOff timeOff) {
        return new TimeOffDTO(
                timeOff.getId(),
                timeOff.getStartsAt(),
                timeOff.getEndsAt(),
                timeOff.getReason()
        );
    }
}
