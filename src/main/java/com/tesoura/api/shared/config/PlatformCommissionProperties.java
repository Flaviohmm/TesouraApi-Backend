package com.tesoura.api.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "app.platform-commission")
public record PlatformCommissionProperties(BigDecimal rate) {
}
