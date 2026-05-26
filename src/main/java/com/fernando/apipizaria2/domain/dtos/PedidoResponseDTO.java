package com.fernando.apipizaria2.domain.dtos;

import org.springframework.hateoas.RepresentationModel;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponseDTO extends RepresentationModel<PedidoResponseDTO> {
    private Long id;
    private LocalDateTime dataHora;
    private String status;
    private Long clienteId;
    private List<ItemPedidoResponseDTO> itens;
}
