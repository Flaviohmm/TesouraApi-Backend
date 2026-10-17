package com.tesoura.api.stripe;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.stripe")
public record StripeProperties(
        String secretKey,
        String webhookSigningSecret,
        String frontendReturnUrl,
        String webhookNotificationUrl
) {
}
