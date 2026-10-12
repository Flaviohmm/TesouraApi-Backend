package com.tesoura.api.publicapi.dto;

import com.tesoura.api.salon.Salon;

import java.util.UUID;

public record PublicSalonSummaryDTO(
        UUID id,
        String name,
        String slug,
        String phone,
        String address,
        String logoUrl
) {

    public static PublicSalonSummaryDTO from(Salon salon) {
        return new PublicSalonSummaryDTO(
                salon.getId(),
                salon.getName(),
                salon.getSlug(),
                salon.getPhone(),
                salon.getAddress(),
                salon.getLogoUrl()
        );
    }
}
