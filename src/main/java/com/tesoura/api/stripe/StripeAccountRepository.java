package com.tesoura.api.stripe;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StripeAccountRepository extends JpaRepository<StripeAccount, UUID> {

    Optional<StripeAccount> findBySalonId(UUID salonId);

    Optional<StripeAccount> findByAccountId(String accountId);
}
