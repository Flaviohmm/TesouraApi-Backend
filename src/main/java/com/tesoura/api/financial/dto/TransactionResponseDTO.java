package com.tesoura.api.financial.dto;

import com.tesoura.api.financial.Transaction;
import com.tesoura.api.shared.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponseDTO(
        UUID id,
        UUID appointmentId,
        TransactionType type,
        BigDecimal amount,
        String description,
        Instant createdAt
) {

    public static TransactionResponseDTO from(Transaction transaction) {
        return new TransactionResponseDTO(
                transaction.getId(),
                transaction.getAppointmentId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }
}
