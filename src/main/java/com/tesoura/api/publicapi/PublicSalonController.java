package com.tesoura.api.publicapi;

import com.tesoura.api.appointment.AppointmentAvailabilityService;
import com.tesoura.api.appointment.dto.AppointmentResponseDTO;
import com.tesoura.api.appointment.dto.AvailableSlotDTO;
import com.tesoura.api.publicapi.dto.PublicAppointmentRequestDTO;
import com.tesoura.api.publicapi.dto.PublicSalonDetailDTO;
import com.tesoura.api.publicapi.dto.PublicSalonSummaryDTO;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/salons")
public class PublicSalonController {

    private final PublicSalonService publicSalonService;
    private final AppointmentAvailabilityService availabilityService;

    public PublicSalonController(
            PublicSalonService publicSalonService,
            AppointmentAvailabilityService availabilityService
    ) {
        this.publicSalonService = publicSalonService;
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public List<PublicSalonSummaryDTO> list() {
        return publicSalonService.listSalons();
    }

    @GetMapping("/{slug}")
    public PublicSalonDetailDTO detail(@PathVariable String slug) {
        return publicSalonService.getDetail(slug);
    }

    @GetMapping("/{slug}/availability")
    public List<AvailableSlotDTO> availability(
            @PathVariable String slug,
            @RequestParam UUID professionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) List<UUID> serviceIds
    ) {
        return availabilityService.availableSlots(professionalId, date, serviceIds);
    }

    @PostMapping("/{slug}/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponseDTO book(
            @PathVariable String slug,
            @Valid @RequestBody PublicAppointmentRequestDTO request
    ) {
        return publicSalonService.book(slug, request);
    }
}
