package com.tesoura.api.professional.dto;

import com.tesoura.api.professional.ProfessionalSchedule;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.UUID;

public record ScheduleDTO(
        UUID id,
        @NotNull @Min(0) @Max(6) Short weekday,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        LocalTime breakStart,
        LocalTime breakEnd
) {

    public static ScheduleDTO from(ProfessionalSchedule schedule) {
        return new ScheduleDTO(
                schedule.getId(),
                schedule.getWeekday(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getBreakStart(),
                schedule.getBreakEnd()
        );
    }
}
