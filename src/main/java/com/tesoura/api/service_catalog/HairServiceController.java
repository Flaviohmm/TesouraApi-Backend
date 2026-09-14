package com.tesoura.api.service_catalog;

import com.tesoura.api.service_catalog.dto.HairServiceRequestDTO;
import com.tesoura.api.service_catalog.dto.HairServiceResponseDTO;
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
@RequestMapping("/api/services")
public class HairServiceController {

    private final HairServiceService hairServiceService;

    public HairServiceController(HairServiceService hairServiceService) {
        this.hairServiceService = hairServiceService;
    }

    @GetMapping
    public List<HairServiceResponseDTO> list() {
        return hairServiceService.list();
    }

    @GetMapping("/{id}")
    public HairServiceResponseDTO get(@PathVariable UUID id) {
        return hairServiceService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HairServiceResponseDTO create(@Valid @RequestBody HairServiceRequestDTO request) {
        return hairServiceService.create(request);
    }

    @PutMapping("/{id}")
    public HairServiceResponseDTO update(@PathVariable UUID id, @Valid @RequestBody HairServiceRequestDTO request) {
        return hairServiceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable UUID id) {
        hairServiceService.deactivate(id);
    }
}
