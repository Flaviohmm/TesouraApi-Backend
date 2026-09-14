package com.tesoura.api.shared.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.EnumeratedValue;

public enum AppointmentStatus {
    SCHEDULED("scheduled"),
    CONFIRMED("confirmed"),
    COMPLETED("completed"),
    NO_SHOW("no_show"),
    CANCELLED("cancelled");

    @EnumeratedValue
    private final String value;

    AppointmentStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AppointmentStatus fromValue(String value) {
        return EnumSupport.fromValue(AppointmentStatus.class, value);
    }
}
