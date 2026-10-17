package com.tesoura.api.stripe;

import com.tesoura.api.shared.tenant.TenantAware;
import com.tesoura.api.shared.tenant.TenantEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stripe_accounts")
@EntityListeners(TenantEntityListener.class)
public class StripeAccount implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "salon_id", nullable = false, updatable = false, unique = true)
    private UUID salonId;

    @Column(name = "account_id", nullable = false, length = 40, unique = true)
    private String accountId;

    @Column(name = "charges_enabled", nullable = false)
    private boolean chargesEnabled;

    @Column(name = "details_submitted", nullable = false)
    private boolean detailsSubmitted;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "pending";

    @Column(name = "connected_at", updatable = false)
    private Instant connectedAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @Override
    public UUID getSalonId() {
        return salonId;
    }

    @Override
    public void setSalonId(UUID salonId) {
        this.salonId = salonId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public boolean isChargesEnabled() {
        return chargesEnabled;
    }

    public void setChargesEnabled(boolean chargesEnabled) {
        this.chargesEnabled = chargesEnabled;
    }

    public boolean isDetailsSubmitted() {
        return detailsSubmitted;
    }

    public void setDetailsSubmitted(boolean detailsSubmitted) {
        this.detailsSubmitted = detailsSubmitted;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(Instant connectedAt) {
        this.connectedAt = connectedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
