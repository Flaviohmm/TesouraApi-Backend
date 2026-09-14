package com.tesoura.api.professional;

import com.tesoura.api.professional.dto.ProfessionalRequestDTO;
import com.tesoura.api.professional.dto.ProfessionalResponseDTO;
import com.tesoura.api.professional.dto.ScheduleDTO;
import com.tesoura.api.professional.dto.TimeOffDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/professionals")
public class ProfessionalController {

    private final ProfessionalService professionalService;

    public ProfessionalController(ProfessionalService professionalService) {
        this.professionalService = professionalService;
    }

    @GetMapping
    public List<ProfessionalResponseDTO> list() {
        return professionalService.list();
    }

    @GetMapping("/{id}")
    public ProfessionalResponseDTO get(@PathVariable UUID id) {
        return professionalService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfessionalResponseDTO create(@Valid @RequestBody ProfessionalRequestDTO request) {
        return professionalService.create(request);
    }

    @PutMapping("/{id}")
    public ProfessionalResponseDTO update(@PathVariable UUID id, @Valid @RequestBody ProfessionalRequestDTO request) {
        return professionalService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable UUID id) {
        professionalService.deactivate(id);
    }

    @PutMapping("/{id}/schedules")
    public List<ScheduleDTO> replaceSchedules(@PathVariable UUID id, @Valid @RequestBody List<ScheduleDTO> schedules) {
        return professionalService.replaceSchedules(id, schedules);
    }

    @GetMapping("/{id}/time-off")
    public List<TimeOffDTO> listTimeOff(@PathVariable UUID id) {
        return professionalService.listTimeOff(id);
    }

    @PostMapping("/{id}/time-off")
    @ResponseStatus(HttpStatus.CREATED)
    public TimeOffDTO addTimeOff(@PathVariable UUID id, @Valid @RequestBody TimeOffDTO request) {
        return professionalService.addTimeOff(id, request);
    }
}
