package com.fernando.apipizaria2.domain.entities;

import com.fernando.apipizaria2.domain.enums.TamanhoProduto;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_produto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    @Column(nullable = false)
    private BigDecimal precoBase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TamanhoProduto tamanho;

    @ManyToMany
    @JoinTable(
        name = "tb_produto_ingrediente",
        joinColumns = @JoinColumn(name = "produto_id"),
        inverseJoinColumns = @JoinColumn(name = "ingrediente_id")
    )
    @Builder.Default
    private List<Ingrediente> ingredientes = new ArrayList<>();
}
