package com.tesoura.api.publicapi.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PublicAppointmentRequestDTO(
        @NotNull UUID professionalId,
        @NotNull Instant startsAt,
        @NotEmpty List<UUID> serviceIds,
        String notes
) {
}
