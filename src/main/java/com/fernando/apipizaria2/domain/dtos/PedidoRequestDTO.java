package com.fernando.apipizaria2.domain.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PedidoRequestDTO(

        @NotNull(message = "ID do cliente obrigatório")
        Long clienteId,

        @NotEmpty(message = "O pedido deve ter pelo menos um item")
        List<@Valid ItemPedidoRequestDTO> itens

) {}
