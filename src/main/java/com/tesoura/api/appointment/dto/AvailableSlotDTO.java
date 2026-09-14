package com.tesoura.api.appointment.dto;

import java.time.Instant;
import java.util.UUID;

public record AvailableSlotDTO(
        Instant startsAt,
        Instant endsAt,
        UUID professionalId
) {
}
