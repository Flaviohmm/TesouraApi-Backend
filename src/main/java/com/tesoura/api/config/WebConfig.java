package com.tesoura.api.config;

import com.tesoura.api.shared.enums.AppointmentStatus;
import com.tesoura.api.shared.enums.NotificationChannel;
import com.tesoura.api.shared.enums.NotificationStatus;
import com.tesoura.api.shared.enums.SubscriptionPlan;
import com.tesoura.api.shared.enums.TransactionType;
import com.tesoura.api.shared.enums.UserRole;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, UserRole.class, UserRole::fromValue);
        registry.addConverter(String.class, AppointmentStatus.class, AppointmentStatus::fromValue);
        registry.addConverter(String.class, TransactionType.class, TransactionType::fromValue);
        registry.addConverter(String.class, SubscriptionPlan.class, SubscriptionPlan::fromValue);
        registry.addConverter(String.class, NotificationChannel.class, NotificationChannel::fromValue);
        registry.addConverter(String.class, NotificationStatus.class, NotificationStatus::fromValue);
    }
}
