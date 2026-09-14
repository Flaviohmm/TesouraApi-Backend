package com.tesoura.api.appointment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AppointmentServiceResponseDTO(
        UUID serviceId,
        String name,
        BigDecimal priceAtBooking
) {
}
