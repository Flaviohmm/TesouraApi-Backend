package com.tesoura.api.shared.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.EnumeratedValue;

public enum SubscriptionPlan {
    SOLO("solo"),
    SALON("salon"),
    TRIAL("trial");

    @EnumeratedValue
    private final String value;

    SubscriptionPlan(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static SubscriptionPlan fromValue(String value) {
        return EnumSupport.fromValue(SubscriptionPlan.class, value);
    }
}
