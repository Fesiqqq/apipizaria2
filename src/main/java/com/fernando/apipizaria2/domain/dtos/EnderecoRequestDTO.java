package com.fernando.apipizaria2.domain.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoRequestDTO(

        @NotBlank(message = "Rua obrigatória")
        @Pattern(regexp = "^[a-zA-ZÀ-ÿ0-9\\s,.\\_\\-]+$", message = "A rua deve conter apenas letras, números e caracteres básicos (,.-)")
        @Size(max = 200, message = "A rua deve ter no máximo 200 caracteres")
        String rua,

        @NotBlank(message = "Número obrigatório")
        @Pattern(regexp = "^[0-9a-zA-Z\\s/\\-]+$", message = "O número deve conter apenas dígitos e letras (ex: 123, 12A, S/N)")
        @Size(max = 20, message = "O número deve ter no máximo 20 caracteres")
        String numero,

        @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s]+$", message = "O bairro deve conter apenas letras e espaços")
        @Size(max = 100, message = "O bairro deve ter no máximo 100 caracteres")
        String bairro,

        @NotBlank(message = "CEP obrigatório")
        @Pattern(regexp = "^\\d{8}$", message = "CEP deve conter exatamente 8 números sem traço (ex: 01310100)")
        String cep,

        @NotNull(message = "ID do cliente obrigatório")
        Long clienteId

) {}
