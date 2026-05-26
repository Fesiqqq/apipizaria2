package com.fernando.apipizaria2.domain.dtos;

import org.springframework.hateoas.RepresentationModel;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnderecoResponseDTO extends RepresentationModel<EnderecoResponseDTO> {
    private Long id;
    private String rua;
    private String numero;
    private String bairro;
    private String cep;
    private Long clienteId;
}
