package com.tesoura.api.webhook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ProcessedWebhookEventRepository extends JpaRepository<ProcessedWebhookEvent, UUID> {

    @Modifying
    @Query(value = "INSERT INTO processed_webhook_events (provider, event_key) VALUES (:provider, :eventKey) "
            + "ON CONFLICT (provider, event_key) DO NOTHING", nativeQuery = true)
    int insertIfAbsent(@Param("provider") String provider, @Param("eventKey") String eventKey);
}
