package com.fernando.apipizaria2.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_endereco")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String rua;

    @Column(nullable = false)
    private String numero;

    private String bairro;

    @Column(nullable = false)
    private String cep;

    @OneToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
}
