package com.tesoura.api.salon;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalonRepository extends JpaRepository<Salon, UUID> {

    boolean existsBySlugIgnoreCase(String slug);

    Optional<Salon> findBySlugIgnoreCase(String slug);

    List<Salon> findBySubscriptionStatusOrderByNameAsc(String subscriptionStatus);
}
