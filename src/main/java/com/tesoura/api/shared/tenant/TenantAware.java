package com.tesoura.api.shared.tenant;

import java.util.UUID;

public interface TenantAware {

    UUID getSalonId();

    void setSalonId(UUID salonId);
}
