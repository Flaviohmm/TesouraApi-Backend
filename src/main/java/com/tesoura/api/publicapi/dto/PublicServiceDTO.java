package com.tesoura.api.publicapi.dto;

import com.tesoura.api.service_catalog.HairService;

import java.math.BigDecimal;
import java.util.UUID;

public record PublicServiceDTO(
        UUID id,
        String name,
        String description,
        int durationMinutes,
        BigDecimal price,
        String category
) {

    public static PublicServiceDTO from(HairService service) {
        return new PublicServiceDTO(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getDurationMinutes(),
                service.getPrice(),
                service.getCategory()
        );
    }
}
