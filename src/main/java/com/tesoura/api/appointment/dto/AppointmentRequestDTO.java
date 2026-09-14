package com.tesoura.api.appointment.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AppointmentRequestDTO(
        @NotNull UUID clientId,
        @NotNull UUID professionalId,
        @NotNull Instant startsAt,
        @NotEmpty List<UUID> serviceIds,
        String notes,
        String createdVia,
        BigDecimal depositAmount,
        Boolean depositPaid
) {
}
