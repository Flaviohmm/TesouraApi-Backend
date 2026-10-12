package com.tesoura.api.customer;

import java.util.UUID;

public record CustomerPrincipal(UUID customerId, String email, String name) {
}
