package com.tesoura.api.salon;

import com.tesoura.api.salon.dto.SalonRequestDTO;
import com.tesoura.api.salon.dto.SalonResponseDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/salons")
public class SalonController {

    private final SalonService salonService;

    public SalonController(SalonService salonService) {
        this.salonService = salonService;
    }

    @GetMapping("/me")
    public SalonResponseDTO me() {
        return salonService.getCurrent();
    }

    @PutMapping("/me")
    public SalonResponseDTO update(@Valid @RequestBody SalonRequestDTO request) {
        return salonService.updateCurrent(request);
    }
}
