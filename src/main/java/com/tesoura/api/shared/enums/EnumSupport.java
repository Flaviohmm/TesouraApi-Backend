package com.tesoura.api.shared.enums;

import java.lang.reflect.Method;
import java.util.Locale;

final class EnumSupport {

    private EnumSupport() {
    }

    static <E extends Enum<E>> E fromValue(Class<E> type, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Valor de enum vazio para " + type.getSimpleName());
        }
        String normalized = value.trim();
        for (E constant : type.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(normalized)
                    || constant.name().replace('_', '-').equalsIgnoreCase(normalized)
                    || constant.name().replace('_', ' ').equalsIgnoreCase(normalized)) {
                return constant;
            }
            String mapped = mappedValue(constant);
            if (mapped != null && mapped.equalsIgnoreCase(normalized)) {
                return constant;
            }
        }
        throw new IllegalArgumentException("Valor inválido '" + value + "' para " + type.getSimpleName());
    }

    private static String mappedValue(Enum<?> constant) {
        try {
            Method method = constant.getClass().getMethod("getValue");
            Object result = method.invoke(constant);
            return result == null ? null : result.toString();
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }

    static String snake(Enum<?> value) {
        return value.name().toLowerCase(Locale.ROOT);
    }
}
