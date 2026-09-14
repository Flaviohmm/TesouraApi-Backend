package com.tesoura.api.client;

import com.tesoura.api.client.dto.ClientRequestDTO;
import com.tesoura.api.client.dto.ClientResponseDTO;
import com.tesoura.api.client.dto.TechnicalNoteRequestDTO;
import com.tesoura.api.client.dto.TechnicalNoteResponseDTO;
import com.tesoura.api.shared.exception.ApiException;
import com.tesoura.api.shared.exception.ResourceNotFoundException;
import com.tesoura.api.shared.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientTechnicalNoteRepository technicalNoteRepository;

    public ClientService(
            ClientRepository clientRepository,
            ClientTechnicalNoteRepository technicalNoteRepository
    ) {
        this.clientRepository = clientRepository;
        this.technicalNoteRepository = technicalNoteRepository;
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDTO> list() {
        return clientRepository.findBySalonIdOrderByNameAsc(TenantContext.get()).stream()
                .map(ClientResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClientResponseDTO get(UUID id) {
        return ClientResponseDTO.from(require(id));
    }

    @Transactional
    public ClientResponseDTO create(ClientRequestDTO request) {
        UUID salonId = TenantContext.get();
        if (clientRepository.existsBySalonIdAndPhone(salonId, request.phone())) {
            throw ApiException.conflict("Já existe um cliente com este telefone");
        }
        Client client = new Client();
        apply(client, request);
        return ClientResponseDTO.from(clientRepository.save(client));
    }

    @Transactional
    public ClientResponseDTO update(UUID id, ClientRequestDTO request) {
        Client client = require(id);
        clientRepository.findBySalonIdAndPhone(TenantContext.get(), request.phone())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw ApiException.conflict("Já existe um cliente com este telefone");
                });
        apply(client, request);
        return ClientResponseDTO.from(clientRepository.save(client));
    }

    @Transactional(readOnly = true)
    public List<TechnicalNoteResponseDTO> listNotes(UUID clientId) {
        require(clientId);
        return technicalNoteRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(TechnicalNoteResponseDTO::from)
                .toList();
    }

    @Transactional
    public TechnicalNoteResponseDTO addNote(UUID clientId, TechnicalNoteRequestDTO request) {
        Client client = require(clientId);
        ClientTechnicalNote note = new ClientTechnicalNote();
        note.setClient(client);
        note.setProfessionalId(request.professionalId());
        note.setTitle(request.title());
        note.setDetails(request.details());
        note.setPhotoUrl(request.photoUrl());
        return TechnicalNoteResponseDTO.from(technicalNoteRepository.save(note));
    }

    public Client require(UUID id) {
        return clientRepository.findByIdAndSalonId(id, TenantContext.get())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
    }

    private void apply(Client client, ClientRequestDTO request) {
        client.setName(request.name());
        client.setPhone(request.phone());
        client.setEmail(request.email());
        client.setBirthday(request.birthday());
        client.setNotes(request.notes());
    }
}
