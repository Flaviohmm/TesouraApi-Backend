package com.tesoura.api.professional;

import com.tesoura.api.professional.dto.ProfessionalRequestDTO;
import com.tesoura.api.professional.dto.ProfessionalResponseDTO;
import com.tesoura.api.professional.dto.ScheduleDTO;
import com.tesoura.api.professional.dto.TimeOffDTO;
import com.tesoura.api.shared.exception.ApiException;
import com.tesoura.api.shared.exception.ResourceNotFoundException;
import com.tesoura.api.shared.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ProfessionalService {

    private final ProfessionalRepository professionalRepository;
    private final ProfessionalScheduleRepository scheduleRepository;
    private final ProfessionalTimeOffRepository timeOffRepository;

    public ProfessionalService(
            ProfessionalRepository professionalRepository,
            ProfessionalScheduleRepository scheduleRepository,
            ProfessionalTimeOffRepository timeOffRepository
    ) {
        this.professionalRepository = professionalRepository;
        this.scheduleRepository = scheduleRepository;
        this.timeOffRepository = timeOffRepository;
    }

    @Transactional(readOnly = true)
    public List<ProfessionalResponseDTO> list() {
        return professionalRepository.findBySalonIdOrderByNameAsc(TenantContext.get()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProfessionalResponseDTO get(UUID id) {
        return toResponse(require(id));
    }

    @Transactional
    public ProfessionalResponseDTO create(ProfessionalRequestDTO request) {
        Professional professional = new Professional();
        apply(professional, request);
        Professional saved = professionalRepository.save(professional);
        replaceSchedules(saved, request.schedules());
        return toResponse(saved);
    }

    @Transactional
    public ProfessionalResponseDTO update(UUID id, ProfessionalRequestDTO request) {
        Professional professional = require(id);
        apply(professional, request);
        replaceSchedules(professional, request.schedules());
        return toResponse(professionalRepository.save(professional));
    }

    @Transactional
    public void deactivate(UUID id) {
        Professional professional = require(id);
        professional.setActive(false);
        professionalRepository.save(professional);
    }

    @Transactional
    public List<ScheduleDTO> replaceSchedules(UUID professionalId, List<ScheduleDTO> schedules) {
        Professional professional = require(professionalId);
        replaceSchedules(professional, schedules);
        return scheduleRepository.findByProfessionalIdOrderByWeekdayAscStartTimeAsc(professional.getId())
                .stream()
                .map(ScheduleDTO::from)
                .toList();
    }

    @Transactional
    public TimeOffDTO addTimeOff(UUID professionalId, TimeOffDTO request) {
        if (!request.endsAt().isAfter(request.startsAt())) {
            throw ApiException.badRequest("Fim do bloqueio deve ser posterior ao início");
        }
        Professional professional = require(professionalId);
        ProfessionalTimeOff timeOff = new ProfessionalTimeOff();
        timeOff.setProfessional(professional);
        timeOff.setStartsAt(request.startsAt());
        timeOff.setEndsAt(request.endsAt());
        timeOff.setReason(request.reason());
        return TimeOffDTO.from(timeOffRepository.save(timeOff));
    }

    @Transactional(readOnly = true)
    public List<TimeOffDTO> listTimeOff(UUID professionalId) {
        require(professionalId);
        return timeOffRepository.findByProfessionalIdOrderByStartsAtAsc(professionalId).stream()
                .map(TimeOffDTO::from)
                .toList();
    }

    public Professional require(UUID id) {
        return professionalRepository.findByIdAndSalonId(id, TenantContext.get())
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));
    }

    private void apply(Professional professional, ProfessionalRequestDTO request) {
        professional.setName(request.name());
        professional.setPhotoUrl(request.photoUrl());
        professional.setBio(request.bio());
        professional.setCommissionRate(request.commissionRate() == null ? BigDecimal.ZERO : request.commissionRate());
        professional.setActive(request.isActive() == null || request.isActive());
        professional.setUserId(request.userId());
    }

    private void replaceSchedules(Professional professional, List<ScheduleDTO> schedules) {
        if (schedules == null) {
            return;
        }
        scheduleRepository.deleteByProfessionalId(professional.getId());
        scheduleRepository.flush();
        for (ScheduleDTO dto : schedules) {
            if (!dto.endTime().isAfter(dto.startTime())) {
                throw ApiException.badRequest("Horário final deve ser posterior ao inicial");
            }
            ProfessionalSchedule schedule = new ProfessionalSchedule();
            schedule.setProfessional(professional);
            schedule.setWeekday(dto.weekday());
            schedule.setStartTime(dto.startTime());
            schedule.setEndTime(dto.endTime());
            schedule.setBreakStart(dto.breakStart());
            schedule.setBreakEnd(dto.breakEnd());
            scheduleRepository.save(schedule);
        }
    }

    private ProfessionalResponseDTO toResponse(Professional professional) {
        List<ScheduleDTO> schedules = scheduleRepository
                .findByProfessionalIdOrderByWeekdayAscStartTimeAsc(professional.getId())
                .stream()
                .map(ScheduleDTO::from)
                .toList();
        return ProfessionalResponseDTO.from(professional, schedules);
    }
}
