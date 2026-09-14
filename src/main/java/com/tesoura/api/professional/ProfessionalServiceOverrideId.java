package com.tesoura.api.professional;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ProfessionalServiceOverrideId implements Serializable {

    @Column(name = "professional_id")
    private UUID professionalId;

    @Column(name = "service_id")
    private UUID serviceId;

    public ProfessionalServiceOverrideId() {
    }

    public ProfessionalServiceOverrideId(UUID professionalId, UUID serviceId) {
        this.professionalId = professionalId;
        this.serviceId = serviceId;
    }

    public UUID getProfessionalId() {
        return professionalId;
    }

    public void setProfessionalId(UUID professionalId) {
        this.professionalId = professionalId;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProfessionalServiceOverrideId that)) {
            return false;
        }
        return Objects.equals(professionalId, that.professionalId) && Objects.equals(serviceId, that.serviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(professionalId, serviceId);
    }
}
