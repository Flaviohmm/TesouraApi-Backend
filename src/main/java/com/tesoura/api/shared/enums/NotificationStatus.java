package com.tesoura.api.shared.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.EnumeratedValue;

public enum NotificationStatus {
    PENDING("pending"),
    SENT("sent"),
    FAILED("failed"),
    DELIVERED("delivered");

    @EnumeratedValue
    private final String value;

    NotificationStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static NotificationStatus fromValue(String value) {
        return EnumSupport.fromValue(NotificationStatus.class, value);
    }
}
