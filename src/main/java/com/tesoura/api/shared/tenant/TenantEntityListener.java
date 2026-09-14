package com.tesoura.api.shared.tenant;

import jakarta.persistence.PrePersist;

public class TenantEntityListener {

    @PrePersist
    public void assignTenant(Object entity) {
        if (entity instanceof TenantAware tenantAware && tenantAware.getSalonId() == null) {
            tenantAware.setSalonId(TenantContext.get());
        }
    }
}
