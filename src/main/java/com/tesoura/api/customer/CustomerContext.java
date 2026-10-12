package com.tesoura.api.customer;

import com.tesoura.api.shared.exception.ApiException;

import java.util.Optional;

public final class CustomerContext {

    private static final ThreadLocal<CustomerPrincipal> CURRENT = new ThreadLocal<>();

    private CustomerContext() {
    }

    public static void set(CustomerPrincipal principal) {
        CURRENT.set(principal);
    }

    public static Optional<CustomerPrincipal> find() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static CustomerPrincipal require() {
        return find().orElseThrow(() -> ApiException.unauthorized("Faça login para continuar"));
    }

    public static void clear() {
        CURRENT.remove();
    }
}
