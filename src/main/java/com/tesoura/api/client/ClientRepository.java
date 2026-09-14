package com.tesoura.api.client;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    List<Client> findBySalonIdOrderByNameAsc(UUID salonId);

    Optional<Client> findByIdAndSalonId(UUID id, UUID salonId);

    boolean existsBySalonIdAndPhone(UUID salonId, String phone);

    Optional<Client> findBySalonIdAndPhone(UUID salonId, String phone);
}
