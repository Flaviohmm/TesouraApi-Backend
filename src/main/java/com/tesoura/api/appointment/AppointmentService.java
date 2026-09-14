package com.tesoura.api.appointment;

import com.tesoura.api.appointment.dto.AppointmentRequestDTO;
import com.tesoura.api.appointment.dto.AppointmentResponseDTO;
import com.tesoura.api.client.Client;
import com.tesoura.api.client.ClientService;
import com.tesoura.api.financial.TransactionService;
import com.tesoura.api.notification.NotificationDispatchService;
import com.tesoura.api.professional.Professional;
import com.tesoura.api.professional.ProfessionalService;
import com.tesoura.api.professional.ProfessionalServiceOverride;
import com.tesoura.api.professional.ProfessionalServiceOverrideRepository;
import com.tesoura.api.service_catalog.HairService;
import com.tesoura.api.service_catalog.HairServiceRepository;
import com.tesoura.api.shared.enums.AppointmentStatus;
import com.tesoura.api.shared.exception.ApiException;
import com.tesoura.api.shared.exception.ResourceNotFoundException;
import com.tesoura.api.shared.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final HairServiceRepository hairServiceRepository;
    private final ProfessionalServiceOverrideRepository overrideRepository;
    private final ClientService clientService;
    private final ProfessionalService professionalService;
    private final NotificationDispatchService notificationDispatchService;
    private final TransactionService transactionService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            HairServiceRepository hairServiceRepository,
            ProfessionalServiceOverrideRepository overrideRepository,
            ClientService clientService,
            ProfessionalService professionalService,
            NotificationDispatchService notificationDispatchService,
            TransactionService transactionService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.hairServiceRepository = hairServiceRepository;
        this.overrideRepository = overrideRepository;
        this.clientService = clientService;
        this.professionalService = professionalService;
        this.notificationDispatchService = notificationDispatchService;
        this.transactionService = transactionService;
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> list(Instant from, Instant to) {
        Instant start = from == null ? Instant.now().minus(7, ChronoUnit.DAYS) : from;
        Instant end = to == null ? Instant.now().plus(30, ChronoUnit.DAYS) : to;
        return appointmentRepository.findInRange(TenantContext.get(), start, end).stream()
                .map(AppointmentResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponseDTO get(UUID id) {
        return AppointmentResponseDTO.from(require(id));
    }

    @Transactional
    public AppointmentResponseDTO create(AppointmentRequestDTO request) {
        Client client = clientService.require(request.clientId());
        Professional professional = professionalService.require(request.professionalId());
        if (!professional.isActive()) {
            throw ApiException.badRequest("Profissional inativo");
        }

        BookingSnapshot snapshot = snapshot(professional.getId(), request.serviceIds());
        Instant endsAt = request.startsAt().plus(snapshot.durationMinutes(), ChronoUnit.MINUTES);
        assertNoConflict(professional.getId(), request.startsAt(), endsAt, null);

        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setProfessional(professional);
        appointment.setStartsAt(request.startsAt());
        appointment.setEndsAt(endsAt);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setNotes(request.notes());
        appointment.setCreatedVia(request.createdVia() == null ? "manual" : request.createdVia());
        appointment.setDepositAmount(request.depositAmount());
        appointment.setDepositPaid(Boolean.TRUE.equals(request.depositPaid()));
        appointment.setTotalPrice(snapshot.totalPrice());
        attachServices(appointment, snapshot.items());

        Appointment saved = appointmentRepository.save(appointment);
        notificationDispatchService.scheduleForAppointment(saved);
        return AppointmentResponseDTO.from(require(saved.getId()));
    }

    @Transactional
    public AppointmentResponseDTO update(UUID id, AppointmentRequestDTO request) {
        Appointment appointment = require(id);
        if (appointment.getStatus() == AppointmentStatus.CANCELLED
                || appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw ApiException.badRequest("Agendamento não pode ser alterado neste status");
        }

        Client client = clientService.require(request.clientId());
        Professional professional = professionalService.require(request.professionalId());
        BookingSnapshot snapshot = snapshot(professional.getId(), request.serviceIds());
        Instant endsAt = request.startsAt().plus(snapshot.durationMinutes(), ChronoUnit.MINUTES);
        assertNoConflict(professional.getId(), request.startsAt(), endsAt, appointment.getId());

        appointment.setClient(client);
        appointment.setProfessional(professional);
        appointment.setStartsAt(request.startsAt());
        appointment.setEndsAt(endsAt);
        appointment.setNotes(request.notes());
        appointment.setDepositAmount(request.depositAmount());
        if (request.depositPaid() != null) {
            appointment.setDepositPaid(request.depositPaid());
        }
        appointment.setTotalPrice(snapshot.totalPrice());
        appointment.getServices().clear();
        attachServices(appointment, snapshot.items());

        appointmentRepository.save(appointment);
        return AppointmentResponseDTO.from(require(appointment.getId()));
    }

    @Transactional
    public AppointmentResponseDTO updateStatus(UUID id, AppointmentStatus status) {
        Appointment appointment = require(id);
        AppointmentStatus previous = appointment.getStatus();
        appointment.setStatus(status);
        appointmentRepository.save(appointment);

        if (status == AppointmentStatus.COMPLETED && previous != AppointmentStatus.COMPLETED) {
            transactionService.recordAppointmentCompletion(appointment);
        }
        if (status == AppointmentStatus.CANCELLED) {
            notificationDispatchService.cancelPendingForAppointment(appointment.getId());
        }
        return AppointmentResponseDTO.from(require(appointment.getId()));
    }

    public Appointment require(UUID id) {
        return appointmentRepository.findByIdAndSalonId(id, TenantContext.get())
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));
    }

    public void assertNoConflict(UUID professionalId, Instant startsAt, Instant endsAt, UUID excludeId) {
        boolean overlap = appointmentRepository.existsOverlap(
                professionalId,
                TenantContext.get(),
                startsAt,
                endsAt,
                AppointmentStatus.CANCELLED,
                excludeId
        );
        if (overlap) {
            throw ApiException.conflict("Horário indisponível para este profissional");
        }
    }

    public BookingSnapshot snapshot(UUID professionalId, List<UUID> serviceIds) {
        UUID salonId = TenantContext.get();
        List<HairService> services = hairServiceRepository.findBySalonIdAndIdIn(salonId, serviceIds);
        if (services.size() != serviceIds.stream().distinct().count()) {
            throw new ResourceNotFoundException("Um ou mais serviços não foram encontrados");
        }

        int duration = 0;
        BigDecimal total = BigDecimal.ZERO;
        List<Line> lines = new java.util.ArrayList<>();
        for (UUID serviceId : serviceIds) {
            HairService service = services.stream()
                    .filter(item -> item.getId().equals(serviceId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));
            if (!service.isActive()) {
                throw ApiException.badRequest("Serviço inativo: " + service.getName());
            }
            ProfessionalServiceOverride override = overrideRepository
                    .findByIdProfessionalIdAndIdServiceId(professionalId, service.getId())
                    .orElse(null);
            BigDecimal price = override != null && override.getCustomPrice() != null
                    ? override.getCustomPrice()
                    : service.getPrice();
            int minutes = override != null && override.getCustomDuration() != null
                    ? override.getCustomDuration()
                    : service.getDurationMinutes();
            duration += minutes;
            total = total.add(price);
            lines.add(new Line(service, price, minutes));
        }
        if (duration <= 0) {
            throw ApiException.badRequest("Duração do agendamento inválida");
        }
        return new BookingSnapshot(duration, total, lines);
    }

    private void attachServices(Appointment appointment, List<Line> lines) {
        for (Line line : lines) {
            AppointmentService_ item = new AppointmentService_();
            item.setAppointment(appointment);
            item.setService(line.service());
            item.setPriceAtBooking(line.price());
            appointment.getServices().add(item);
        }
    }

    public record Line(HairService service, BigDecimal price, int durationMinutes) {
    }

    public record BookingSnapshot(int durationMinutes, BigDecimal totalPrice, List<Line> items) {
    }
}
