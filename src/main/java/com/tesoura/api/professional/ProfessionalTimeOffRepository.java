package com.tesoura.api.professional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ProfessionalTimeOffRepository extends JpaRepository<ProfessionalTimeOff, UUID> {

    List<ProfessionalTimeOff> findByProfessionalIdOrderByStartsAtAsc(UUID professionalId);

    @Query("""
            SELECT t FROM ProfessionalTimeOff t
            WHERE t.professional.id = :professionalId
              AND t.startsAt < :rangeEnd
              AND t.endsAt > :rangeStart
            """)
    List<ProfessionalTimeOff> findOverlapping(
            @Param("professionalId") UUID professionalId,
            @Param("rangeStart") Instant rangeStart,
            @Param("rangeEnd") Instant rangeEnd
    );
}
