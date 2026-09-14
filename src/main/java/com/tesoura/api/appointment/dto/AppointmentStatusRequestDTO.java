package com.tesoura.api.appointment.dto;

import com.tesoura.api.shared.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record AppointmentStatusRequestDTO(
        @NotNull AppointmentStatus status
) {
}
