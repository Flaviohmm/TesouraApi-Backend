package com.tesoura.api.appointment.dto;

import com.tesoura.api.appointment.Appointment;
import com.tesoura.api.appointment.AppointmentService_;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AppointmentResponseDTO(
        UUID id,
        UUID clientId,
        String clientName,
        UUID professionalId,
        String professionalName,
        Instant startsAt,
        Instant endsAt,
        String status,
        boolean depositPaid,
        BigDecimal depositAmount,
        BigDecimal totalPrice,
        String notes,
        String createdVia,
        List<AppointmentServiceResponseDTO> services,
        Instant createdAt
) {

    public static AppointmentResponseDTO from(Appointment appointment) {
        List<AppointmentServiceResponseDTO> services = appointment.getServices().stream()
                .map(AppointmentResponseDTO::toService)
                .toList();
        return new AppointmentResponseDTO(
                appointment.getId(),
                appointment.getClient().getId(),
                appointment.getClient().getName(),
                appointment.getProfessional().getId(),
                appointment.getProfessional().getName(),
                appointment.getStartsAt(),
                appointment.getEndsAt(),
                appointment.getStatus().getValue(),
                appointment.isDepositPaid(),
                appointment.getDepositAmount(),
                appointment.getTotalPrice(),
                appointment.getNotes(),
                appointment.getCreatedVia(),
                services,
                appointment.getCreatedAt()
        );
    }

    private static AppointmentServiceResponseDTO toService(AppointmentService_ item) {
        return new AppointmentServiceResponseDTO(
                item.getService().getId(),
                item.getService().getName(),
                item.getPriceAtBooking()
        );
    }
}
