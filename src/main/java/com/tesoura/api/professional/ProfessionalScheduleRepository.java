package com.tesoura.api.professional;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfessionalScheduleRepository extends JpaRepository<ProfessionalSchedule, UUID> {

    List<ProfessionalSchedule> findByProfessionalIdOrderByWeekdayAscStartTimeAsc(UUID professionalId);

    Optional<ProfessionalSchedule> findByProfessionalIdAndWeekday(UUID professionalId, short weekday);

    void deleteByProfessionalId(UUID professionalId);
}
