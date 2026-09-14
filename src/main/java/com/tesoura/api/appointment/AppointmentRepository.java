package com.tesoura.api.appointment;

import com.tesoura.api.shared.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    @Query("""
            SELECT DISTINCT a FROM Appointment a
            JOIN FETCH a.client
            JOIN FETCH a.professional
            LEFT JOIN FETCH a.services s
            LEFT JOIN FETCH s.service
            WHERE a.salonId = :salonId
              AND a.startsAt >= :from
              AND a.startsAt < :to
            ORDER BY a.startsAt
            """)
    List<Appointment> findInRange(
            @Param("salonId") UUID salonId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("""
            SELECT DISTINCT a FROM Appointment a
            JOIN FETCH a.client
            JOIN FETCH a.professional
            LEFT JOIN FETCH a.services s
            LEFT JOIN FETCH s.service
            WHERE a.id = :id AND a.salonId = :salonId
            """)
    Optional<Appointment> findByIdAndSalonId(@Param("id") UUID id, @Param("salonId") UUID salonId);

    @Query("""
            SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END
            FROM Appointment a
            WHERE a.professional.id = :professionalId
              AND a.salonId = :salonId
              AND a.status <> :cancelled
              AND a.startsAt < :endsAt
              AND a.endsAt > :startsAt
              AND (:excludeId IS NULL OR a.id <> :excludeId)
            """)
    boolean existsOverlap(
            @Param("professionalId") UUID professionalId,
            @Param("salonId") UUID salonId,
            @Param("startsAt") Instant startsAt,
            @Param("endsAt") Instant endsAt,
            @Param("cancelled") AppointmentStatus cancelled,
            @Param("excludeId") UUID excludeId
    );

    @Query("""
            SELECT a FROM Appointment a
            WHERE a.professional.id = :professionalId
              AND a.salonId = :salonId
              AND a.status <> :cancelled
              AND a.startsAt < :rangeEnd
              AND a.endsAt > :rangeStart
            ORDER BY a.startsAt
            """)
    List<Appointment> findOverlappingForProfessional(
            @Param("professionalId") UUID professionalId,
            @Param("salonId") UUID salonId,
            @Param("rangeStart") Instant rangeStart,
            @Param("rangeEnd") Instant rangeEnd,
            @Param("cancelled") AppointmentStatus cancelled
    );
}
