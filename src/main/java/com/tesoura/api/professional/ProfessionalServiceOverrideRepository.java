package com.tesoura.api.professional;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfessionalServiceOverrideRepository extends JpaRepository<ProfessionalServiceOverride, ProfessionalServiceOverrideId> {

    Optional<ProfessionalServiceOverride> findByIdProfessionalIdAndIdServiceId(UUID professionalId, UUID serviceId);

    List<ProfessionalServiceOverride> findByIdProfessionalId(UUID professionalId);
}
