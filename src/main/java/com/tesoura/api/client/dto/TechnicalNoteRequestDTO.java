package com.tesoura.api.client.dto;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TechnicalNoteRequestDTO(
        UUID professionalId,
        @Size(max = 120) String title,
        String details,
        String photoUrl
) {
}
