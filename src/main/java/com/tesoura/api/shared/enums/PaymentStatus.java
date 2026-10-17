package com.tesoura.api.shared.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.EnumeratedValue;

public enum PaymentStatus {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected"),
    REFUNDED("refunded"),
    CANCELLED("cancelled");

    @EnumeratedValue
    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PaymentStatus fromValue(String value) {
        return EnumSupport.fromValue(PaymentStatus.class, value);
    }
}
