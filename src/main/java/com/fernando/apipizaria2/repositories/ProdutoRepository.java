package com.fernando.apipizaria2.repositories;

import com.fernando.apipizaria2.domain.entities.Produto;
import com.fernando.apipizaria2.domain.enums.TamanhoProduto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    Page<Produto> findByTamanho(TamanhoProduto tamanho, Pageable pageable);
}
