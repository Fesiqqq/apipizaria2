package com.fernando.apipizaria2.domain.dtos;

import org.springframework.hateoas.RepresentationModel;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoResponseDTO extends RepresentationModel<ItemPedidoResponseDTO> {
    private Long id;
    private Integer quantidade;
    private BigDecimal precoUnitario;
    private Long produtoId;
    private String produtoNome;
}
