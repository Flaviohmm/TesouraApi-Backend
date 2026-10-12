package com.tesoura.api.publicapi.dto;

import com.tesoura.api.salon.Salon;

import java.util.List;
import java.util.UUID;

public record PublicSalonDetailDTO(
        UUID id,
        String name,
        String slug,
        String phone,
        String address,
        String timezone,
        String logoUrl,
        List<PublicServiceDTO> services,
        List<PublicProfessionalDTO> professionals
) {

    public static PublicSalonDetailDTO from(
            Salon salon,
            List<PublicServiceDTO> services,
            List<PublicProfessionalDTO> professionals
    ) {
        return new PublicSalonDetailDTO(
                salon.getId(),
                salon.getName(),
                salon.getSlug(),
                salon.getPhone(),
                salon.getAddress(),
                salon.getTimezone(),
                salon.getLogoUrl(),
                services,
                professionals
        );
    }
}
