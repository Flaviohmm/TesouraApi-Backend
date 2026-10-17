package com.tesoura.api.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByExternalReference(String externalReference);

    Optional<Payment> findTopByAppointmentIdAndSalonIdOrderByCreatedAtDesc(UUID appointmentId, UUID salonId);
}
