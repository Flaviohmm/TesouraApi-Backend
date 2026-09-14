package com.tesoura.api.professional;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "professional_services")
public class ProfessionalServiceOverride {

    @EmbeddedId
    private ProfessionalServiceOverrideId id;

    @Column(name = "custom_price", precision = 10, scale = 2)
    private BigDecimal customPrice;

    @Column(name = "custom_duration")
    private Integer customDuration;

    public ProfessionalServiceOverrideId getId() {
        return id;
    }

    public void setId(ProfessionalServiceOverrideId id) {
        this.id = id;
    }

    public BigDecimal getCustomPrice() {
        return customPrice;
    }

    public void setCustomPrice(BigDecimal customPrice) {
        this.customPrice = customPrice;
    }

    public Integer getCustomDuration() {
        return customDuration;
    }

    public void setCustomDuration(Integer customDuration) {
        this.customDuration = customDuration;
    }
}
