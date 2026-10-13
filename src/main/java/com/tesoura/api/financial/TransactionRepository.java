package com.tesoura.api.financial;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findTop50BySalonIdOrderByCreatedAtDesc(UUID salonId);
}
