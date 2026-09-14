package com.tesoura.api.shared.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.EnumeratedValue;

public enum NotificationChannel {
    WHATSAPP("whatsapp"),
    SMS("sms"),
    EMAIL("email");

    @EnumeratedValue
    private final String value;

    NotificationChannel(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static NotificationChannel fromValue(String value) {
        return EnumSupport.fromValue(NotificationChannel.class, value);
    }
}
