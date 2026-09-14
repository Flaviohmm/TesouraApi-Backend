package com.tesoura.api.client.dto;

import com.tesoura.api.client.Client;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ClientResponseDTO(
        UUID id,
        String name,
        String phone,
        String email,
        LocalDate birthday,
        String notes,
        int loyaltyPoints,
        Instant createdAt
) {

    public static ClientResponseDTO from(Client client) {
        return new ClientResponseDTO(
                client.getId(),
                client.getName(),
                client.getPhone(),
                client.getEmail(),
                client.getBirthday(),
                client.getNotes(),
                client.getLoyaltyPoints(),
                client.getCreatedAt()
        );
    }
}
