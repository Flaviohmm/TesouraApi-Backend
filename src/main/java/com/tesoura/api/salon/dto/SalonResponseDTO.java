package com.tesoura.api.salon.dto;

import com.tesoura.api.salon.Salon;

import java.time.Instant;
import java.util.UUID;

public record SalonResponseDTO(
        UUID id,
        String name,
        String slug,
        String phone,
        String address,
        String timezone,
        String logoUrl,
        String subscriptionPlan,
        String subscriptionStatus,
        Instant trialEndsAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static SalonResponseDTO from(Salon salon) {
        return new SalonResponseDTO(
                salon.getId(),
                salon.getName(),
                salon.getSlug(),
                salon.getPhone(),
                salon.getAddress(),
                salon.getTimezone(),
                salon.getLogoUrl(),
                salon.getSubscriptionPlan() == null ? null : salon.getSubscriptionPlan().getValue(),
                salon.getSubscriptionStatus(),
                salon.getTrialEndsAt(),
                salon.getCreatedAt(),
                salon.getUpdatedAt()
        );
    }
}
