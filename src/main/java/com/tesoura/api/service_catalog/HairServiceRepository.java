package com.tesoura.api.service_catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HairServiceRepository extends JpaRepository<HairService, UUID> {

    List<HairService> findBySalonIdOrderByNameAsc(UUID salonId);

    Optional<HairService> findByIdAndSalonId(UUID id, UUID salonId);

    List<HairService> findBySalonIdAndIdIn(UUID salonId, List<UUID> ids);
}
