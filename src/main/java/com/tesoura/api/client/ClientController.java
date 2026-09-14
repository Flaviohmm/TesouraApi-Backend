package com.tesoura.api.client;

import com.tesoura.api.client.dto.ClientRequestDTO;
import com.tesoura.api.client.dto.ClientResponseDTO;
import com.tesoura.api.client.dto.TechnicalNoteRequestDTO;
import com.tesoura.api.client.dto.TechnicalNoteResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public List<ClientResponseDTO> list() {
        return clientService.list();
    }

    @GetMapping("/{id}")
    public ClientResponseDTO get(@PathVariable UUID id) {
        return clientService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponseDTO create(@Valid @RequestBody ClientRequestDTO request) {
        return clientService.create(request);
    }

    @PutMapping("/{id}")
    public ClientResponseDTO update(@PathVariable UUID id, @Valid @RequestBody ClientRequestDTO request) {
        return clientService.update(id, request);
    }

    @GetMapping("/{id}/technical-notes")
    public List<TechnicalNoteResponseDTO> listNotes(@PathVariable UUID id) {
        return clientService.listNotes(id);
    }

    @PostMapping("/{id}/technical-notes")
    @ResponseStatus(HttpStatus.CREATED)
    public TechnicalNoteResponseDTO addNote(
            @PathVariable UUID id,
            @Valid @RequestBody TechnicalNoteRequestDTO request
    ) {
        return clientService.addNote(id, request);
    }
}
