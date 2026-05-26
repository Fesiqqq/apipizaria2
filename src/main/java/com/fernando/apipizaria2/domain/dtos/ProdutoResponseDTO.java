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
public class ProdutoResponseDTO extends RepresentationModel<ProdutoResponseDTO> {
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal precoBase;
    private String tamanho;
}
