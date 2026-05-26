package com.fernando.apipizaria2.domain.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemPedidoRequestDTO(
        @NotNull(message = "ID do produto obrigatório") Long produtoId,
        @NotNull(message = "Quantidade obrigatória") @Positive(message = "Quantidade deve ser maior que zero") Integer quantidade
) {}
