package com.tesoura.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 120) String salonName,
        @NotBlank
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug deve conter apenas letras minúsculas, números e hífen")
        @Size(max = 80)
        String slug,
        @NotBlank @Size(max = 120) String ownerName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 80) String password,
        @Size(max = 20) String phone
) {
}
