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
public class ClienteResponseDTO extends RepresentationModel<ClienteResponseDTO> {
    private Long id;
    private String nome;
    private String email;
    private String telefone;
}
