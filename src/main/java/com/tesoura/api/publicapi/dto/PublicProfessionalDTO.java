package com.tesoura.api.publicapi.dto;

import com.tesoura.api.professional.Professional;

import java.util.UUID;

public record PublicProfessionalDTO(
        UUID id,
        String name,
        String photoUrl,
        String bio
) {

    public static PublicProfessionalDTO from(Professional professional) {
        return new PublicProfessionalDTO(
                professional.getId(),
                professional.getName(),
                professional.getPhotoUrl(),
                professional.getBio()
        );
    }
}
