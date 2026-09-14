package com.tesoura.api.appointment;

import com.tesoura.api.appointment.dto.AppointmentRequestDTO;
import com.tesoura.api.appointment.dto.AppointmentResponseDTO;
import com.tesoura.api.appointment.dto.AppointmentStatusRequestDTO;
import com.tesoura.api.appointment.dto.AvailableSlotDTO;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentAvailabilityService availabilityService;

    public AppointmentController(
            AppointmentService appointmentService,
            AppointmentAvailabilityService availabilityService
    ) {
        this.appointmentService = appointmentService;
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public List<AppointmentResponseDTO> list(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        return appointmentService.list(from, to);
    }

    @GetMapping("/availability")
    public List<AvailableSlotDTO> availability(
            @RequestParam UUID professionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) List<UUID> serviceIds
    ) {
        return availabilityService.availableSlots(professionalId, date, serviceIds);
    }

    @GetMapping("/{id}")
    public AppointmentResponseDTO get(@PathVariable UUID id) {
        return appointmentService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponseDTO create(@Valid @RequestBody AppointmentRequestDTO request) {
        return appointmentService.create(request);
    }

    @PutMapping("/{id}")
    public AppointmentResponseDTO update(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentRequestDTO request
    ) {
        return appointmentService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public AppointmentResponseDTO updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentStatusRequestDTO request
    ) {
        return appointmentService.updateStatus(id, request.status());
    }
}
