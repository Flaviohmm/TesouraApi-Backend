package com.tesoura.api.shared.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class DateTimeUtils {

    public static final ZoneId DEFAULT_ZONE = ZoneId.of("America/Recife");

    private DateTimeUtils() {
    }

    public static ZoneId zoneOf(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return DEFAULT_ZONE;
        }
        try {
            return ZoneId.of(timezone);
        } catch (Exception ex) {
            return DEFAULT_ZONE;
        }
    }

    public static Instant toInstant(LocalDate date, LocalTime time, ZoneId zone) {
        return ZonedDateTime.of(date, time, zone).toInstant();
    }

    public static LocalDate toLocalDate(Instant instant, ZoneId zone) {
        return instant.atZone(zone).toLocalDate();
    }

    public static LocalTime toLocalTime(Instant instant, ZoneId zone) {
        return instant.atZone(zone).toLocalTime();
    }

    public static int weekdaySundayZero(LocalDate date) {
        return date.getDayOfWeek().getValue() % 7;
    }
}
