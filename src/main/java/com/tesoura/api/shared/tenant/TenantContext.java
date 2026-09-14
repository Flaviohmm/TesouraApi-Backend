package com.tesoura.api.shared.tenant;

import java.util.Optional;
import java.util.UUID;

public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_SALON = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(UUID salonId) {
        CURRENT_SALON.set(salonId);
    }

    public static UUID get() {
        UUID salonId = CURRENT_SALON.get();
        if (salonId == null) {
            throw new IllegalStateException("Tenant context não foi definido");
        }
        return salonId;
    }

    public static Optional<UUID> find() {
        return Optional.ofNullable(CURRENT_SALON.get());
    }

    public static void clear() {
        CURRENT_SALON.remove();
    }
}
