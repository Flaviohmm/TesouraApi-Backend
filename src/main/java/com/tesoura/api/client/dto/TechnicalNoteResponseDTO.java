package com.tesoura.api.client.dto;

import com.tesoura.api.client.ClientTechnicalNote;

import java.time.Instant;
import java.util.UUID;

public record TechnicalNoteResponseDTO(
        UUID id,
        UUID clientId,
        UUID professionalId,
        String title,
        String details,
        String photoUrl,
        Instant createdAt
) {

    public static TechnicalNoteResponseDTO from(ClientTechnicalNote note) {
        return new TechnicalNoteResponseDTO(
                note.getId(),
                note.getClient().getId(),
                note.getProfessionalId(),
                note.getTitle(),
                note.getDetails(),
                note.getPhotoUrl(),
                note.getCreatedAt()
        );
    }
}
