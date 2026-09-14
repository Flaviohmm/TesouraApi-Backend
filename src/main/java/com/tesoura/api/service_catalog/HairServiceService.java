package com.tesoura.api.service_catalog;

import com.tesoura.api.service_catalog.dto.HairServiceRequestDTO;
import com.tesoura.api.service_catalog.dto.HairServiceResponseDTO;
import com.tesoura.api.shared.exception.ResourceNotFoundException;
import com.tesoura.api.shared.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class HairServiceService {

    private final HairServiceRepository hairServiceRepository;

    public HairServiceService(HairServiceRepository hairServiceRepository) {
        this.hairServiceRepository = hairServiceRepository;
    }

    @Transactional(readOnly = true)
    public List<HairServiceResponseDTO> list() {
        return hairServiceRepository.findBySalonIdOrderByNameAsc(TenantContext.get()).stream()
                .map(HairServiceResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public HairServiceResponseDTO get(UUID id) {
        return HairServiceResponseDTO.from(require(id));
    }

    @Transactional
    public HairServiceResponseDTO create(HairServiceRequestDTO request) {
        HairService service = new HairService();
        apply(service, request);
        return HairServiceResponseDTO.from(hairServiceRepository.save(service));
    }

    @Transactional
    public HairServiceResponseDTO update(UUID id, HairServiceRequestDTO request) {
        HairService service = require(id);
        apply(service, request);
        return HairServiceResponseDTO.from(hairServiceRepository.save(service));
    }

    @Transactional
    public void deactivate(UUID id) {
        HairService service = require(id);
        service.setActive(false);
        hairServiceRepository.save(service);
    }

    public HairService require(UUID id) {
        return hairServiceRepository.findByIdAndSalonId(id, TenantContext.get())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));
    }

    private void apply(HairService service, HairServiceRequestDTO request) {
        service.setName(request.name());
        service.setDescription(request.description());
        service.setDurationMinutes(request.durationMinutes());
        service.setPrice(request.price());
        service.setCategory(request.category());
        service.setActive(request.isActive() == null || request.isActive());
    }
}
