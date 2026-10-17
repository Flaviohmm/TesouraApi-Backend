package com.tesoura.api.stripe.dto;

public record StripeAccountStatusDTO(boolean connected, boolean chargesEnabled, String accountId) {

    public static StripeAccountStatusDTO disconnected() {
        return new StripeAccountStatusDTO(false, false, null);
    }
}
