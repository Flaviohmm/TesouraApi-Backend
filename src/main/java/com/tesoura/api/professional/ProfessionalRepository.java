package com.tesoura.api.professional;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfessionalRepository extends JpaRepository<Professional, UUID> {

    List<Professional> findBySalonIdOrderByNameAsc(UUID salonId);

    Optional<Professional> findByIdAndSalonId(UUID id, UUID salonId);
}
