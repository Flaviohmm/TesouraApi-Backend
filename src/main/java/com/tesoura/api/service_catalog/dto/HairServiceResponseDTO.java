package com.tesoura.api.service_catalog.dto;

import com.tesoura.api.service_catalog.HairService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record HairServiceResponseDTO(
        UUID id,
        String name,
        String description,
        int durationMinutes,
        BigDecimal price,
        String category,
        boolean isActive,
        Instant createdAt
) {

    public static HairServiceResponseDTO from(HairService service) {
        return new HairServiceResponseDTO(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getDurationMinutes(),
                service.getPrice(),
                service.getCategory(),
                service.isActive(),
                service.getCreatedAt()
        );
    }
}
