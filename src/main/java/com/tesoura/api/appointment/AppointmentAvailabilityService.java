package com.tesoura.api.appointment;

import com.tesoura.api.appointment.dto.AvailableSlotDTO;
import com.tesoura.api.professional.Professional;
import com.tesoura.api.professional.ProfessionalSchedule;
import com.tesoura.api.professional.ProfessionalScheduleRepository;
import com.tesoura.api.professional.ProfessionalService;
import com.tesoura.api.professional.ProfessionalTimeOff;
import com.tesoura.api.professional.ProfessionalTimeOffRepository;
import com.tesoura.api.salon.SalonService;
import com.tesoura.api.shared.enums.AppointmentStatus;
import com.tesoura.api.shared.tenant.TenantContext;
import com.tesoura.api.shared.util.DateTimeUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentAvailabilityService {

    private static final int SLOT_STEP_MINUTES = 15;

    private final AppointmentRepository appointmentRepository;
    private final ProfessionalService professionalService;
    private final ProfessionalScheduleRepository scheduleRepository;
    private final ProfessionalTimeOffRepository timeOffRepository;
    private final AppointmentService appointmentService;
    private final SalonService salonService;

    public AppointmentAvailabilityService(
            AppointmentRepository appointmentRepository,
            ProfessionalService professionalService,
            ProfessionalScheduleRepository scheduleRepository,
            ProfessionalTimeOffRepository timeOffRepository,
            AppointmentService appointmentService,
            SalonService salonService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.professionalService = professionalService;
        this.scheduleRepository = scheduleRepository;
        this.timeOffRepository = timeOffRepository;
        this.appointmentService = appointmentService;
        this.salonService = salonService;
    }

    @Transactional(readOnly = true)
    public List<AvailableSlotDTO> availableSlots(UUID professionalId, LocalDate date, List<UUID> serviceIds) {
        Professional professional = professionalService.require(professionalId);
        if (!professional.isActive()) {
            return List.of();
        }

        ZoneId zone = DateTimeUtils.zoneOf(salonService.currentSalon().getTimezone());
        short weekday = (short) DateTimeUtils.weekdaySundayZero(date);
        ProfessionalSchedule schedule = scheduleRepository
                .findByProfessionalIdAndWeekday(professionalId, weekday)
                .orElse(null);
        if (schedule == null) {
            return List.of();
        }

        int durationMinutes = serviceIds == null || serviceIds.isEmpty()
                ? 30
                : appointmentService.snapshot(professionalId, serviceIds).durationMinutes();

        Instant dayStart = DateTimeUtils.toInstant(date, LocalTime.MIN, zone);
        Instant dayEnd = DateTimeUtils.toInstant(date.plusDays(1), LocalTime.MIN, zone);

        List<Appointment> busy = appointmentRepository.findOverlappingForProfessional(
                professionalId,
                TenantContext.get(),
                dayStart,
                dayEnd,
                AppointmentStatus.CANCELLED
        );
        List<ProfessionalTimeOff> timeOffs = timeOffRepository.findOverlapping(professionalId, dayStart, dayEnd);

        List<AvailableSlotDTO> slots = new ArrayList<>();
        Instant cursor = DateTimeUtils.toInstant(date, schedule.getStartTime(), zone);
        Instant workEnd = DateTimeUtils.toInstant(date, schedule.getEndTime(), zone);
        Instant now = Instant.now();

        Instant breakStart = schedule.getBreakStart() == null
                ? null
                : DateTimeUtils.toInstant(date, schedule.getBreakStart(), zone);
        Instant breakEnd = schedule.getBreakEnd() == null
                ? null
                : DateTimeUtils.toInstant(date, schedule.getBreakEnd(), zone);

        while (!cursor.plus(Duration.ofMinutes(durationMinutes)).isAfter(workEnd)) {
            Instant slotStart = cursor;
            Instant slotEnd = slotStart.plus(Duration.ofMinutes(durationMinutes));
            boolean inBreak = breakStart != null && breakEnd != null
                    && slotStart.isBefore(breakEnd) && slotEnd.isAfter(breakStart);
            boolean overlapsAppointment = busy.stream()
                    .anyMatch(appointment -> overlaps(slotStart, slotEnd, appointment.getStartsAt(), appointment.getEndsAt()));
            boolean overlapsTimeOff = timeOffs.stream()
                    .anyMatch(off -> overlaps(slotStart, slotEnd, off.getStartsAt(), off.getEndsAt()));
            if (!inBreak && !overlapsAppointment && !overlapsTimeOff && slotEnd.isAfter(now)) {
                slots.add(new AvailableSlotDTO(slotStart, slotEnd, professionalId));
            }
            cursor = cursor.plus(Duration.ofMinutes(SLOT_STEP_MINUTES));
        }
        return slots;
    }

    private boolean overlaps(Instant start, Instant end, Instant otherStart, Instant otherEnd) {
        return start.isBefore(otherEnd) && end.isAfter(otherStart);
    }
}
