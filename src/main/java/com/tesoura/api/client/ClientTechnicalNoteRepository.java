package com.tesoura.api.client;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClientTechnicalNoteRepository extends JpaRepository<ClientTechnicalNote, UUID> {

    List<ClientTechnicalNote> findByClientIdOrderByCreatedAtDesc(UUID clientId);
}
