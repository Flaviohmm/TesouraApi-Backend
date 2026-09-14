package com.tesoura.api.salon;

import com.tesoura.api.salon.dto.SalonRequestDTO;
import com.tesoura.api.salon.dto.SalonResponseDTO;
import com.tesoura.api.shared.exception.ResourceNotFoundException;
import com.tesoura.api.shared.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalonService {

    private final SalonRepository salonRepository;

    public SalonService(SalonRepository salonRepository) {
        this.salonRepository = salonRepository;
    }

    @Transactional(readOnly = true)
    public SalonResponseDTO getCurrent() {
        return SalonResponseDTO.from(currentSalon());
    }

    @Transactional
    public SalonResponseDTO updateCurrent(SalonRequestDTO request) {
        Salon salon = currentSalon();
        salon.setName(request.name());
        salon.setPhone(request.phone());
        salon.setAddress(request.address());
        if (request.timezone() != null && !request.timezone().isBlank()) {
            salon.setTimezone(request.timezone());
        }
        salon.setLogoUrl(request.logoUrl());
        return SalonResponseDTO.from(salonRepository.save(salon));
    }

    public Salon currentSalon() {
        return salonRepository.findById(TenantContext.get())
                .orElseThrow(() -> new ResourceNotFoundException("Salão não encontrado"));
    }
}
