package com.fernando.apipizaria2.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_api_key")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String chave;

    @Column(nullable = false)
    private String dono; // Ex: Nome do sistema ou pessoa que solicitou

    @Column(nullable = false)
    private boolean ativo;

    private LocalDateTime dataCriacao;
}
