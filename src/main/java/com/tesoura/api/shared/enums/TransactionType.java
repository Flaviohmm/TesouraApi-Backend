package com.tesoura.api.shared.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.EnumeratedValue;

public enum TransactionType {
    INCOME("income"),
    EXPENSE("expense"),
    COMMISSION("commission");

    @EnumeratedValue
    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TransactionType fromValue(String value) {
        return EnumSupport.fromValue(TransactionType.class, value);
    }
}
