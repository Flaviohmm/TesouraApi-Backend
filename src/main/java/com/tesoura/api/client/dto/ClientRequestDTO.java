package com.tesoura.api.client.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ClientRequestDTO(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 20) String phone,
        @Email @Size(max = 160) String email,
        LocalDate birthday,
        String notes
) {
}
