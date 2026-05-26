package com.fernando.apipizaria2.domain.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProdutoRequestDTO(

        @NotBlank(message = "Nome obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        String descricao,

        @NotNull(message = "Preço obrigatório")
        @Positive(message = "Preço deve ser positivo")
        BigDecimal precoBase,

        @NotBlank(message = "Tamanho obrigatório")
        @Pattern(regexp = "^(PEQUENO|MEDIO|GRANDE|FAMILIA|UNICO)$", message = "Tamanho inválido. Valores aceitos: PEQUENO, MEDIO, GRANDE, FAMILIA, UNICO")
        String tamanho

) {}
