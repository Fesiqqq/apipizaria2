package com.fernando.apipizaria2.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_ingrediente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    private String descricao;
}
